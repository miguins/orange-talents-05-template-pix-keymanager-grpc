package br.com.zup.lucasmiguins.pix.consulta

import br.com.zup.lucasmiguins.grpc.ConsultaChavePixRequest
import br.com.zup.lucasmiguins.grpc.KeymanagerConsultaGrpcServiceGrpc
import br.com.zup.lucasmiguins.integration.bcb.BancoCentralClient
import br.com.zup.lucasmiguins.integration.bcb.consulta.PixKeyDetailsResponse
import br.com.zup.lucasmiguins.integration.bcb.registra.CreatePixKeyRequest
import br.com.zup.lucasmiguins.pix.ChavePix
import br.com.zup.lucasmiguins.pix.ChavePixRepository
import br.com.zup.lucasmiguins.pix.ContaAssociada
import br.com.zup.lucasmiguins.pix.enums.EnumTipoDeChave.*
import br.com.zup.lucasmiguins.pix.enums.EnumTipoDeConta
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
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

@MicronautTest(transactional = false)
internal class ConsultaChaveEndpointTest(
    private val repository: ChavePixRepository,
    private val grpcClient: KeymanagerConsultaGrpcServiceGrpc.KeymanagerConsultaGrpcServiceBlockingStub,
) {

    @Inject
    lateinit var bcbClient: BancoCentralClient

    companion object {
       private val CLIENTE_ID = UUID.randomUUID()
    }

    @BeforeEach
    fun setup() {
        repository.save(chavePix(tipo = EMAIL, chave = "ponte@email.com", clienteId = CLIENTE_ID))
        repository.save(chavePix(tipo = CPF, chave = "34528318091", clienteId = UUID.randomUUID()))
        repository.save(chavePix(tipo = ALEATORIA, chave = "aleatoria123-chave", clienteId = CLIENTE_ID))
        repository.save(chavePix(tipo = CELULAR, chave = "+5591912345678", clienteId = CLIENTE_ID))
    }

    @AfterEach
    fun cleanUp() {
        repository.deleteAll()
    }

    // happy path 1
    @Test
    internal fun `deve retornar uma chave pix existente por valor da chave`() {
        // cenário
        val chaveExistente = repository.findByChave(chave = "ponte@email.com").get()

        //ação
        val response = grpcClient.consulta(
            ConsultaChavePixRequest.newBuilder()
                .setChave(chaveExistente.chave)
                .build()
        )

        //validação
        with(response) {
            assertEquals(chaveExistente.id.toString(), pixId)
            assertEquals(chaveExistente.chave, chave.chave)
        }
    }

    // happy path 2
    @Test
    internal fun `deve retornar uma chave pix existente por pixId e clienteId`() {
        // cenário
        val chaveExistente = repository.findByChave(chave = "34528318091").get()

        //ação
        val response = grpcClient.consulta(
            ConsultaChavePixRequest.newBuilder()
                .setPixId(
                    ConsultaChavePixRequest.FiltroPorPixId.newBuilder()
                        .setPixId(chaveExistente.id.toString())
                        .setClienteId(chaveExistente.clienteId.toString())
                        .build()
                )
                .build()
        )

        //validação
        with(response) {
            assertEquals(chaveExistente.id.toString(), pixId)
            assertEquals(chaveExistente.chave, chave.chave)
        }
    }

    @Test
    internal fun `deve retornar quando existir apenas no bcb`() {
        val bcbResponse = pixKeyDetailsResponse()

        `when`(bcbClient.consultaPorChave(key = "chave-usuario-banco")).thenReturn(HttpResponse.ok(bcbResponse))

        val response = grpcClient.consulta(
            ConsultaChavePixRequest.newBuilder()
                .setChave("chave-usuario-banco")
                .build()
        )

        with(response) {
            assertEquals(bcbResponse.key, chave.chave)
            assertEquals(bcbResponse.keyType.name, response.chave.tipo.name)
            assertEquals("", response.pixId)
            assertEquals("", response.clienteId)
        }
    }

    @Test
    internal fun `nao deve retornar quando chave nula`() {

        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.consulta(
                ConsultaChavePixRequest.newBuilder()
                    .setPixId(
                        ConsultaChavePixRequest.FiltroPorPixId.newBuilder()
                            .setPixId("")
                            .setClienteId("")
                            .build()
                    )
                    .build()
            )
        }

        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
        }
    }

    @Test
    internal fun `nao deve retornar quando pixId conter atributos invalidos`() {
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.consulta(
                ConsultaChavePixRequest.newBuilder()
                    .setChave("")
                    .build()
            )
        }

        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
        }
    }

    @Test
    internal fun `nao deve retornar quando chave nao existir no bcb`() {
        `when`(bcbClient.consultaPorChave(key = "chave-usuario-nao-existente")).thenReturn(HttpResponse.badRequest())

        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.consulta(
                ConsultaChavePixRequest.newBuilder()
                    .setChave("chave-usuario-nao-existente")
                    .build()
            )
        }

        with(thrown) {
            assertEquals(Status.NOT_FOUND.code, status.code)
        }
    }

    @Factory
    class ClientConsultaChaveEndpointTest {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeymanagerConsultaGrpcServiceGrpc.KeymanagerConsultaGrpcServiceBlockingStub {
            return KeymanagerConsultaGrpcServiceGrpc.newBlockingStub(channel)
        }
    }

    @MockBean(BancoCentralClient::class)
    fun bcbClient(): BancoCentralClient? {
        return Mockito.mock(BancoCentralClient::class.java)
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
            tipoDeConta = EnumTipoDeConta.CONTA_CORRENTE,
            conta = ContaAssociada(
                instituicao = "UNIBANCO ITAU",
                nomeDoTitular = "Rafael Ponte",
                cpfDoTitular = "34528318091",
                agencia = "1218",
                numeroDaConta = "123456"
            )
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

    private fun pixKeyDetailsResponse(): PixKeyDetailsResponse {
        return PixKeyDetailsResponse(
            keyType = CreatePixKeyRequest.PixKeyType.EMAIL,
            key = "chave-usuario-banco",
            bankAccount = bankAccount(),
            owner = owner(),
            createdAt = LocalDateTime.now()
        )
    }
}