package br.com.zup.lucasmiguins.pix.registra

import br.com.zup.lucasmiguins.grpc.KeymanagerRegistraGrpcServiceGrpc
import br.com.zup.lucasmiguins.grpc.RegistraChavePixRequest
import br.com.zup.lucasmiguins.grpc.RegistraChavePixResponse
import br.com.zup.lucasmiguins.shared.handlers.ErrorAroundHandler
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Validator

@Singleton
@ErrorAroundHandler
class RegistraChaveEndpoint(
    @Inject private val service: NovaChavePixService,
    private val validator: Validator
) : KeymanagerRegistraGrpcServiceGrpc.KeymanagerRegistraGrpcServiceImplBase() {

    override fun registra(
        request: RegistraChavePixRequest,
        responseObserver: StreamObserver<RegistraChavePixResponse>
    ) {
        val novaChavePix = request.toNovaChavePix(validator)
        val chaveCriada = service.registra(novaChavePix)

        responseObserver.onNext(
            RegistraChavePixResponse.newBuilder()
                .setClienteId(chaveCriada.clienteId.toString())
                .setPixId(chaveCriada.id.toString())
                .build()
        )
        responseObserver.onCompleted()
    }
}