package br.com.zup.lucasmiguins.pix.remove

import br.com.zup.lucasmiguins.grpc.KeymanagerRemoveGrpcServiceGrpc
import br.com.zup.lucasmiguins.grpc.RemoveChavePixRequest
import br.com.zup.lucasmiguins.integration.bcb.BancoCentralClient
import br.com.zup.lucasmiguins.integration.bcb.remove.DeletePixKeyRequest
import br.com.zup.lucasmiguins.integration.bcb.remove.DeletePixKeyResponse
import br.com.zup.lucasmiguins.pix.ChavePix
import br.com.zup.lucasmiguins.pix.ChavePixRepository
import br.com.zup.lucasmiguins.pix.ContaAssociada
import br.com.zup.lucasmiguins.pix.enums.EnumTipoDeChave
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
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

@MicronautTest(transactional = false)
internal class RemoveChaveEndpointTest(
    private val chavePixRepository: ChavePixRepository,
    private val grpcClient: KeymanagerRemoveGrpcServiceGrpc.KeymanagerRemoveGrpcServiceBlockingStub
) {
    @Inject
    lateinit var bcbClient: BancoCentralClient

    private lateinit var chavePixExistente: ChavePix

    @BeforeEach
    internal fun setUp() {
        chavePixExistente = chavePixRepository.save(
            chavePix(
                tipo = EnumTipoDeChave.EMAIL,
                chave = "ponte@email.com",
                clienteId = UUID.randomUUID()
            )
        )
    }

    @AfterEach
    internal fun tearDown() {
        chavePixRepository.deleteAll()
    }

    // happy path
    @Test
    internal fun `deve remover uma chave pix existente`() {

        // cenario
        `when`(bcbClient.deletarChavePix("ponte@email.com", DeletePixKeyRequest("ponte@email.com")))
            .thenReturn(HttpResponse.ok(DeletePixKeyResponse(key = "ponte@email.com",
                        participant = ContaAssociada.ITAU_UNIBANCO_ISPB,
                        deletedAt = LocalDateTime.now())
                )
            )

        // acao
        val response = grpcClient.remove(
            RemoveChavePixRequest.newBuilder()
                .setClienteId(chavePixExistente.clienteId.toString())
                .setPixId(chavePixExistente.id.toString())
                .build()
        )

        // validacao
        with(response) {
            assertEquals(chavePixExistente.clienteId.toString(), clienteId)
            assertEquals(chavePixExistente.id.toString(), pixId)
        }
    }

    @Test
    internal fun `nao deve remover uma chave pix inexistente`() {
        val thrown = org.junit.jupiter.api.assertThrows<StatusRuntimeException> {
            grpcClient.remove(
                RemoveChavePixRequest.newBuilder()
                    .setClienteId(chavePixExistente.clienteId.toString())
                    .setPixId(UUID.randomUUID().toString())
                    .build()
            )
        }

        with(thrown) {
            assertEquals(Status.NOT_FOUND.code, status.code)
        }
    }

    @Test
    internal fun `nao deve remover uma chave pix de outro cliente`() {
        val thrown = org.junit.jupiter.api.assertThrows<StatusRuntimeException> {
            grpcClient.remove(
                RemoveChavePixRequest.newBuilder()
                    .setClienteId(UUID.randomUUID().toString())
                    .setPixId(chavePixExistente.id.toString())
                    .build()
            )
        }

        with(thrown) {
            assertEquals(Status.NOT_FOUND.code, status.code)
        }
    }
    @Test
    internal fun `nao deve remover uma chave pix quando nao preenchida`() {
        val thrown = org.junit.jupiter.api.assertThrows<StatusRuntimeException> {
            grpcClient.remove(
                RemoveChavePixRequest.newBuilder()
                    .setClienteId("")
                    .setPixId("")
                    .build()
            )
        }

        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
        }
    }

    @Test
    internal fun `nao deve remover uma chave pix quando UUID invalido`() {
        val thrown = org.junit.jupiter.api.assertThrows<StatusRuntimeException> {
            grpcClient.remove(
                RemoveChavePixRequest.newBuilder()
                    .setClienteId(UUID.randomUUID().toString() + "abc")
                    .setPixId(UUID.randomUUID().toString() + "def")
                    .build()
            )
        }

        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
        }
    }

    @Factory
    class ClientRemoveChaveEndpointTest {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeymanagerRemoveGrpcServiceGrpc.KeymanagerRemoveGrpcServiceBlockingStub {
            return KeymanagerRemoveGrpcServiceGrpc.newBlockingStub(channel)
        }
    }

    @MockBean(BancoCentralClient::class)
    fun bcbClient(): BancoCentralClient? {
        return Mockito.mock(BancoCentralClient::class.java)
    }

    private fun chavePix(
        tipo: EnumTipoDeChave,
        chave: String = UUID.randomUUID().toString(),
        clienteId: UUID = UUID.randomUUID(),
    ): ChavePix {
        return ChavePix(
            clienteId = clienteId,
            tipoDeChave = tipo,
            chave = chave,
            tipoDeConta = EnumTipoDeConta.CONTA_CORRENTE,
            ContaAssociada(
                instituicao = "UNIBANCO ITAU SA",
                nomeDoTitular = "Rafael Ponte",
                cpfDoTitular = "34528318091",
                agencia = "1218",
                numeroDaConta = "291900"
            )
        )
    }
}