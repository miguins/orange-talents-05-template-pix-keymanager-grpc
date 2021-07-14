package br.com.zup.lucasmiguins.pix.registra

import br.com.zup.lucasmiguins.pix.enums.EnumTipoDeChave
import br.com.zup.lucasmiguins.pix.enums.EnumTipoDeConta
import br.com.zup.lucasmiguins.grpc.RegistraChavePixRequest
import br.com.zup.lucasmiguins.grpc.EnumTipoDeChave.*
import br.com.zup.lucasmiguins.grpc.EnumTipoDeConta.*
import javax.validation.ConstraintViolationException
import javax.validation.Validator

fun RegistraChavePixRequest.toNovaChavePix(validador: Validator) : NovaChavePix {

    val novaChavePix = NovaChavePix(

        clienteId = this.clienteId,
        chave = this.chave,

        tipoDeChave = when (this.tipoDeChave) {
            UNKNOWN_TIPO_CHAVE -> null
            else -> EnumTipoDeChave.valueOf(this.tipoDeChave.name)
        },
        tipoDeConta = when (this.tipoDeConta) {
            UNKNOWN_TIPO_CONTA -> null
            else -> EnumTipoDeConta.valueOf(this.tipoDeConta.name)
        },
    )

    val erros = validador.validate(novaChavePix)
    if (erros.isNotEmpty()) {
        throw ConstraintViolationException(erros)
    }

    return novaChavePix
}