package br.com.zup.lucasmiguins.pix.registra

import br.com.zup.lucasmiguins.pix.ChavePix
import br.com.zup.lucasmiguins.pix.ContaAssociada
import br.com.zup.lucasmiguins.pix.enums.EnumTipoDeChave
import br.com.zup.lucasmiguins.pix.enums.EnumTipoDeConta
import br.com.zup.lucasmiguins.shared.validation.ValidUUID
import io.micronaut.core.annotation.Introspected
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@ValidPixKey
@Introspected
data class NovaChavePix(

    @field:NotBlank
    @field:ValidUUID
    val clienteId: String?,

    @field:NotNull
    val tipoDeChave: EnumTipoDeChave?,

    @field:NotNull
    @field:Size(max = 77)
    val chave: String?,

    @field:NotNull
    val tipoDeConta: EnumTipoDeConta?

) {

    fun toChavePix(conta: ContaAssociada): ChavePix {
        return ChavePix(
            clienteId = UUID.fromString(clienteId),
            tipoDeChave = EnumTipoDeChave.valueOf(tipoDeChave!!.name),
            chave = if (this.tipoDeChave == EnumTipoDeChave.ALEATORIA) UUID.randomUUID().toString() else this.chave!!,
            tipoDeConta = EnumTipoDeConta.valueOf(tipoDeConta!!.name),
            conta = conta
        )
    }
}