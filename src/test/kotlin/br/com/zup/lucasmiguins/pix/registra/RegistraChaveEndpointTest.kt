package br.com.zup.lucasmiguins.pix.registra

import br.com.zup.lucasmiguins.grpc.EnumTipoDeChave
import br.com.zup.lucasmiguins.grpc.EnumTipoDeConta
import br.com.zup.lucasmiguins.grpc.KeymanagerRegistraGrpcServiceGrpc
import br.com.zup.lucasmiguins.grpc.RegistraChavePixRequest
import br.com.zup.lucasmiguins.integration.bcb.BancoCentralClient
import br.com.zup.lucasmiguins.integration.bcb.registra.CreatePixKeyRequest
import br.com.zup.lucasmiguins.integration.bcb.registra.CreatePixKeyResponse
import br.com.zup.lucasmiguins.integration.itau.ContasClientesItauClient
import br.com.zup.lucasmiguins.integration.itau.DadosContaResponse
import br.com.zup.lucasmiguins.integration.itau.InstituicaoResponse
import br.com.zup.lucasmiguins.integration.itau.TitularResponse
import br.com.zup.lucasmiguins.pix.ChavePix
import br.com.zup.lucasmiguins.pix.ChavePixRepository
import br.com.zup.lucasmiguins.pix.ContaAssociada
import br.com.zup.lucasmiguins.pix.enums.EnumTipoDeChave.CPF
import br.com.zup.lucasmiguins.pix.enums.EnumTipoDeConta.CONTA_CORRENTE
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

@MicronautTest(transactional = false)
internal class RegistraChaveEndpointTest(
    private val chavePixRepository: ChavePixRepository,
    val grpcClient: KeymanagerRegistraGrpcServiceGrpc.KeymanagerRegistraGrpcServiceBlockingStub
) {

    @Inject
    lateinit var itauClient: ContasClientesItauClient

    @Inject
    lateinit var bcbClient: BancoCentralClient

    companion object {
        val CLIENTE_ID: UUID = UUID.randomUUID()
    }

    @BeforeEach
    internal fun setUp() {
        chavePixRepository.deleteAll()
    }

    // happy path
    @Test
    internal fun `deve registrar um nova chave pix`() {
        // cenário
        `when`(itauClient.buscaContaPorTipo(clienteId = CLIENTE_ID.toString(), tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(dadosDaContaResponse()))

        `when`(bcbClient.criarChavePix(createPixKeyRequest()))
            .thenReturn(HttpResponse.created(createPixKeyResponse()))

        // ação
        val response = grpcClient.registra(
            RegistraChavePixRequest.newBuilder()
                .setClienteId(CLIENTE_ID.toString())
                .setTipoDeChave(EnumTipoDeChave.EMAIL)
                .setChave("ponte@email.com")
                .setTipoDeConta(EnumTipoDeConta.CONTA_CORRENTE)
                .build()
        )

        // validação
        with(response) {
            assertEquals(CLIENTE_ID.toString(), clienteId)
            assertNotNull(pixId)
        }
    }

    @Test
    internal fun `nao deve registrar uma chave pix ja existente`() {
        val novaChave = chavePix(
            tipo = CPF,
            chave = "47888036074",
            clienteId = CLIENTE_ID
        )

        chavePixRepository.save(novaChave)

        val thrown = assertThrows<StatusRuntimeException> {

            grpcClient.registra(
                RegistraChavePixRequest.newBuilder()
                    .setClienteId(CLIENTE_ID.toString())
                    .setTipoDeChave(EnumTipoDeChave.valueOf(novaChave.tipoDeChave.name))
                    .setChave(novaChave.chave)
                    .setTipoDeConta(EnumTipoDeConta.valueOf(novaChave.tipoDeConta.name))
                    .build()
            )
        }

        with(thrown) {
            assertEquals(Status.ALREADY_EXISTS.code, this.status.code)
        }
    }

    @Test
    internal fun `nao deve registrar chave pix para cliente nao existente`() {
        `when`(itauClient.buscaContaPorTipo(clienteId = CLIENTE_ID.toString(), tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.notFound())

        `when`(bcbClient.criarChavePix(createPixKeyRequest()))
            .thenReturn(HttpResponse.badRequest())

        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.registra(
                RegistraChavePixRequest.newBuilder()
                    .setClienteId(CLIENTE_ID.toString())
                    .setTipoDeChave(EnumTipoDeChave.EMAIL)
                    .setChave("ponte@email.com")
                    .setTipoDeConta(EnumTipoDeConta.CONTA_CORRENTE)
                    .build()
            )
        }

        with(thrown) {
            assertEquals(Status.INTERNAL.code, this.status.code)
        }
    }

    @Factory
    class ClientRegistraChaveEndpointTest {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeymanagerRegistraGrpcServiceGrpc.KeymanagerRegistraGrpcServiceBlockingStub {
            return KeymanagerRegistraGrpcServiceGrpc.newBlockingStub(channel)
        }
    }

    @MockBean(ContasClientesItauClient::class)
    fun itauClient(): ContasClientesItauClient? {
        return Mockito.mock(ContasClientesItauClient::class.java)
    }

    @MockBean(BancoCentralClient::class)
    fun bcbClient(): BancoCentralClient? {
        return Mockito.mock(BancoCentralClient::class.java)
    }

    private fun dadosDaContaResponse(): DadosContaResponse {
        return DadosContaResponse(
            tipo = "CONTA_CORRENTE",
            instituicao = InstituicaoResponse("UNIBANCO ITAU SA", ContaAssociada.ITAU_UNIBANCO_ISPB),
            agencia = "1218",
            numero = "291900",
            titular = TitularResponse("Rafael Ponte", "34528318091")
        )
    }

    private fun contaAssociada(): ContaAssociada {
        return dadosDaContaResponse().toModel()
    }

    private fun chavePix(
        tipo: br.com.zup.lucasmiguins.pix.enums.EnumTipoDeChave,
        chave: String = UUID.randomUUID().toString(),
        clienteId: UUID = UUID.randomUUID(),
    ): ChavePix {
        return ChavePix(
            clienteId = clienteId,
            tipoDeChave = tipo,
            chave = chave,
            tipoDeConta = CONTA_CORRENTE,
            contaAssociada()
        )
    }

    private fun createPixKeyRequest(): CreatePixKeyRequest {
        return CreatePixKeyRequest(
            keyType = CreatePixKeyRequest.PixKeyType.EMAIL,
            key = "ponte@email.com",
            bankAccount = bankAccount(),
            owner = owner()
        )
    }

    private fun createPixKeyResponse(): CreatePixKeyResponse {
        return CreatePixKeyResponse(
            keyType = CreatePixKeyRequest.PixKeyType.EMAIL,
            key = "ponte@email.com",
            bankAccount = bankAccount(),
            owner = owner(),
            createdAt = LocalDateTime.now()
        )
    }

    private fun bankAccount(): CreatePixKeyRequest.BankAccount {
        return CreatePixKeyRequest.BankAccount(
            participant = ContaAssociada.ITAU_UNIBANCO_ISPB,
            branch = "1218",
            accountNumber = "291900",
            accountType = CreatePixKeyRequest.BankAccount.AccountType.CACC
        )
    }

    private fun owner(): CreatePixKeyRequest.Owner {
        return CreatePixKeyRequest.Owner(
            type = CreatePixKeyRequest.Owner.OwnerType.NATURAL_PERSON,
            name = "Rafael Ponte",
            taxIdNumber = "34528318091"
        )
    }
}