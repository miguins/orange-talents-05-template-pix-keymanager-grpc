package br.com.zup.lucasmiguins.pix.consulta

import br.com.zup.lucasmiguins.pix.ChavePix
import br.com.zup.lucasmiguins.pix.ContaAssociada
import br.com.zup.lucasmiguins.pix.enums.EnumTipoDeChave
import br.com.zup.lucasmiguins.pix.enums.EnumTipoDeConta
import java.time.LocalDateTime
import java.util.*

data class ChavePixInfo(
    val pixId: UUID? = null,
    val clienteId: UUID? = null,
    val tipo: EnumTipoDeChave,
    val chave: String,
    val tipoDeConta: EnumTipoDeConta,
    val conta: ContaAssociada,
    val registradaEm: LocalDateTime = LocalDateTime.now()
) {

    companion object {
        fun of(chave: ChavePix): ChavePixInfo {
            return ChavePixInfo(
                pixId = chave.id,
                clienteId = chave.clienteId,
                tipo = chave.tipoDeChave,
                chave = chave.chave,
                tipoDeConta = chave.tipoDeConta,
                conta = chave.conta,
                registradaEm = chave.criadaEm
            )
        }
    }
}