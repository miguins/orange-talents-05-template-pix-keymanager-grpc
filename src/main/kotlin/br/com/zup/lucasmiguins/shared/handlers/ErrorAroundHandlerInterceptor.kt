package br.com.zup.lucasmiguins.shared.handlers

import br.com.zup.lucasmiguins.pix.exceptions.ChavePixClienteNaoEncontradaException
import br.com.zup.lucasmiguins.pix.exceptions.ChavePixExistenteException
import io.grpc.Status
import io.grpc.stub.StreamObserver
import io.micronaut.aop.InterceptorBean
import io.micronaut.aop.MethodInterceptor
import io.micronaut.aop.MethodInvocationContext
import javax.inject.Singleton
import javax.validation.ConstraintViolationException

@Singleton
@InterceptorBean(ErrorAroundHandler::class)
class ErrorAroundHandlerInterceptor : MethodInterceptor<Any, Any> {

    override fun intercept(context: MethodInvocationContext<Any, Any>): Any? {

        try {

            return context.proceed()
        } catch (ex: Exception) {

            // [1] pois o response observer é o segundo parâmetro no grpc
            val responseObserver = context.parameterValues[1] as StreamObserver<*>

            val status = when (ex) {
                is ConstraintViolationException -> Status.INVALID_ARGUMENT.withCause(ex).withDescription(ex.message)
                is ChavePixExistenteException -> Status.ALREADY_EXISTS.withCause(ex).withDescription(ex.message)
                is IllegalStateException -> Status.NOT_FOUND.withCause(ex).withDescription(ex.message)
                is ChavePixClienteNaoEncontradaException -> Status.NOT_FOUND.withCause(ex).withDescription(ex.message)

                else -> Status.UNKNOWN.withCause(ex).withDescription("Ocorreu um erro inesperado")
            }

            responseObserver.onError(status.asRuntimeException())
        }

        return null
    }

}