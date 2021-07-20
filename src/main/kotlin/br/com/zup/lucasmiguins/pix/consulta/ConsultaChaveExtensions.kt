package br.com.zup.lucasmiguins.pix.consulta

import br.com.zup.lucasmiguins.grpc.ConsultaChavePixRequest
import br.com.zup.lucasmiguins.grpc.ConsultaChavePixRequest.FiltroCase.*
import javax.validation.ConstraintViolationException
import javax.validation.Validator

fun ConsultaChavePixRequest.toModel(validator: Validator): Filtro {

    val filtro = when(filtroCase!!) {
        PIXID -> pixId.let {
            Filtro.PorPixId(clienteId = it.clienteId, pixId = it.pixId)
        }
        CHAVE -> Filtro.PorChave(chave)
        FILTRO_NOT_SET -> Filtro.Invalido()
    }

    val violations = validator.validate(filtro)
    if (violations.isNotEmpty()) {
        throw ConstraintViolationException(violations);
    }

    return filtro
}
