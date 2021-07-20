package br.com.zup.lucasmiguins.pix.lista

import org.junit.jupiter.api.Assertions.*
import br.com.zup.lucasmiguins.grpc.KeymanagerListaGrpcServiceGrpc
import br.com.zup.lucasmiguins.grpc.ListaChavesPixRequest
import br.com.zup.lucasmiguins.integration.bcb.BancoCentralClient
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
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.apache.commons.compress.archivers.sevenz.CLI
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*
import javax.inject.Inject

@MicronautTest(transactional = false)
internal class ListaChavesEndpointTest(
    private val repository: ChavePixRepository,
    private val grpcClient: KeymanagerListaGrpcServiceGrpc.KeymanagerListaGrpcServiceBlockingStub,
) {

    @Inject
    lateinit var bcbClient: BancoCentralClient

    companion object {
        private val CLIENTE_ID = UUID.randomUUID()
    }

    @BeforeEach
    fun setup() {
        repository.save(chavePix(tipo = EnumTipoDeChave.CPF, chave = "34528318091", clienteId = UUID.randomUUID()))
        repository.save(chavePix(tipo = EnumTipoDeChave.EMAIL, chave = "ponte@email.com", clienteId = CLIENTE_ID))
        repository.save(chavePix(tipo = EnumTipoDeChave.ALEATORIA,chave = "aleatoria123-chave",clienteId = CLIENTE_ID))
        repository.save(chavePix(tipo = EnumTipoDeChave.CELULAR, chave = "+5591912345678", clienteId = CLIENTE_ID))
    }

    @AfterEach
    fun cleanUp() {
        repository.deleteAll()
    }


    // happy path
    @Test
    internal fun `deve retornar chave de lista de clientes`() {

        // acao
        val response = grpcClient.lista(ListaChavesPixRequest.newBuilder()
                .setClienteId(CLIENTE_ID.toString())
                .build()
        )

        with(response.chavesList) {
            assertEquals(this.size, 3)
            assertTrue(contains(this.find { it.chave == "ponte@email.com" }))
            assertTrue(contains(this.find { it.chave == "aleatoria123-chave" }))
            assertTrue(contains(this.find { it.chave == "+5591912345678" }))
            assertFalse(contains(this.find { it.chave == "naoexiste" }))
        }
    }

    @Test
    internal fun `nao deve retornar quando cliente nao possuir chaves`() {

        val clienteSemChave = UUID.randomUUID().toString()

        val response = grpcClient.lista(ListaChavesPixRequest.newBuilder()
            .setClienteId(clienteSemChave)
            .build()
        )

        with(response.chavesList) {
            assertEquals(this.size, 0)
        }
    }

    @Test
    internal fun `nao deve retornar quando clienteId for invalido`() {

        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.lista(ListaChavesPixRequest.newBuilder()
                .setClienteId("")
                .build())
        }

        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
        }
    }

    @Factory
    class ClientListaChavesEndpointTest {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeymanagerListaGrpcServiceGrpc.KeymanagerListaGrpcServiceBlockingStub {
            return KeymanagerListaGrpcServiceGrpc.newBlockingStub(channel)
        }
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
}