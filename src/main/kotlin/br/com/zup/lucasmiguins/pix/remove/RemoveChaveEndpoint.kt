package br.com.zup.lucasmiguins.pix.remove

import br.com.zup.lucasmiguins.grpc.KeymanagerRemoveGrpcServiceGrpc
import br.com.zup.lucasmiguins.grpc.RemoveChavePixRequest
import br.com.zup.lucasmiguins.grpc.RemoveChavePixResponse
import br.com.zup.lucasmiguins.shared.handlers.ErrorAroundHandler
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

@ErrorAroundHandler
@Singleton
class RemoveChaveEndpoint(
    @Inject private val service: RemoveChavePixService,
) : KeymanagerRemoveGrpcServiceGrpc.KeymanagerRemoveGrpcServiceImplBase() {

    override fun remove(request: RemoveChavePixRequest, responseObserver: StreamObserver<RemoveChavePixResponse>) {

        service.remove(clienteId = request.clienteId, pixId = request.pixId)

        responseObserver.onNext(
            RemoveChavePixResponse.newBuilder()
                .setClienteId(request.clienteId.toString())
                .setPixId(request.pixId.toString())
                .build()
        )
        responseObserver.onCompleted()
    }
}
