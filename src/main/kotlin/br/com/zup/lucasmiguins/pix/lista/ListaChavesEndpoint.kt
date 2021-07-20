package br.com.zup.lucasmiguins.pix.lista

import br.com.zup.lucasmiguins.grpc.*
import br.com.zup.lucasmiguins.pix.ChavePixRepository
import br.com.zup.lucasmiguins.shared.handlers.ErrorAroundHandler
import com.google.protobuf.Timestamp
import io.grpc.stub.StreamObserver
import java.lang.IllegalArgumentException
import java.time.ZoneId
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@ErrorAroundHandler
@Singleton
class ListaChavesEndpoint(

    @Inject private val repository: ChavePixRepository
) : KeymanagerListaGrpcServiceGrpc.KeymanagerListaGrpcServiceImplBase() {

    override fun lista(
        request: ListaChavesPixRequest,
        responseObserver: StreamObserver<ListaChavesPixResponse>,
    ) {

        if (request.clienteId.isNullOrBlank())
            throw IllegalArgumentException("clienteId: não pode ser nulo ou vazio")

        val clienteId = UUID.fromString(request.clienteId)

        // Mapear para o objeto de resposta
        val chaves = repository.findAllByClienteId(clienteId).map {
            ListaChavesPixResponse.ChavePix.newBuilder()
                .setPixId(it.id.toString())
                .setTipo(EnumTipoDeChave.valueOf(it.tipoDeChave.name))
                .setChave(it.chave)
                .setTipoDeConta(EnumTipoDeConta.valueOf(it.tipoDeConta.name))
                .setCriadaEm(it.criadaEm.let {
                    val createdAt = it.atZone(ZoneId.of("UTC")).toInstant()
                    Timestamp.newBuilder()
                        .setSeconds(createdAt.epochSecond)
                        .setNanos(createdAt.nano)
                        .build()
                })
                .build()
        }

        responseObserver.onNext(ListaChavesPixResponse.newBuilder()
            .setClienteId(clienteId.toString())
            .addAllChaves(chaves)
            .build())
        responseObserver.onCompleted()
    }
}