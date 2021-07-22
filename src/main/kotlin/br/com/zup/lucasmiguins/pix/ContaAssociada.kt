package br.com.zup.lucasmiguins.pix

import javax.persistence.*
import javax.validation.constraints.NotBlank

@Entity
@Table(name = "conta_associada")
class ContaAssociada(

    @field:NotBlank
    @Column( name = "instituicao", nullable = false)
    val instituicao: String,

    @field:NotBlank
    @Column( name = "nome_titular", nullable = false)
    val nomeDoTitular: String,

    @field:NotBlank
    @Column( name = "cpf_titular", nullable = false)
    val cpfDoTitular: String,

    @field:NotBlank
    @Column( name = "agencia", nullable = false)
    val agencia: String,

    @field:NotBlank
    @Column( name = "numero_conta", nullable = false)
    val numeroDaConta: String
) {

    @Id
    @Column(name = "id_conta_associada")
    @SequenceGenerator(name = "sq_conta_associada", sequenceName = "sq_conta_associada", allocationSize = 1)
    @GeneratedValue(generator = "sq_conta_associada", strategy = GenerationType.SEQUENCE)
    private val id: Long? = null

    companion object {
        const val ITAU_UNIBANCO_ISPB: String = "60701190"
    }
}