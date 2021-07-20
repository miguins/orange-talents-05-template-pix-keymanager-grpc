package br.com.zup.lucasmiguins.pix.consulta

import br.com.zup.lucasmiguins.grpc.ConsultaChavePixRequest
import br.com.zup.lucasmiguins.grpc.ConsultaChavePixResponse
import br.com.zup.lucasmiguins.grpc.KeymanagerConsultaGrpcServiceGrpc
import br.com.zup.lucasmiguins.integration.bcb.BancoCentralClient
import br.com.zup.lucasmiguins.pix.ChavePixRepository
import br.com.zup.lucasmiguins.shared.handlers.ErrorAroundHandler
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Validator

@ErrorAroundHandler
@Singleton
class ConsultaChaveEndpoint(

    @Inject private val repository: ChavePixRepository,
    @Inject private val bcbClient: BancoCentralClient,
    @Inject private val validator: Validator
): KeymanagerConsultaGrpcServiceGrpc.KeymanagerConsultaGrpcServiceImplBase() {

    override fun consulta(
        request: ConsultaChavePixRequest,
        responseObserver: StreamObserver<ConsultaChavePixResponse>
    ) {

        val filtro = request.toModel(validator)
        val chaveInfo = filtro.filtra(repository = repository, bcbClient = bcbClient)

        responseObserver.onNext(ConsultaChavePixResponseConverter().convert(chaveInfo))
        responseObserver.onCompleted()
    }
}