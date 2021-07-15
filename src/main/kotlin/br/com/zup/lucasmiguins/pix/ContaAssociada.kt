package br.com.zup.lucasmiguins.pix

import javax.persistence.Embeddable
import javax.validation.constraints.NotBlank

@Embeddable
class ContaAssociada(
    @field:NotBlank
    val instituicao: String,

    @field:NotBlank
    val nomeDoTitular: String,

    @field:NotBlank
    val cpfDoTitular: String,

    @field:NotBlank
    val agencia: String,

    @field:NotBlank
    val numeroDaConta: String
) {

    companion object {
        const val ITAU_UNIBANCO_ISPB: String = "60701190"
    }
}