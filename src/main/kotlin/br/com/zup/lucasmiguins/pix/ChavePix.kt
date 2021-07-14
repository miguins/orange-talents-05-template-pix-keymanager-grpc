package br.com.zup.lucasmiguins.pix

import br.com.zup.lucasmiguins.pix.enums.EnumTipoDeChave
import br.com.zup.lucasmiguins.pix.enums.EnumTipoDeConta
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Entity
@Table(
    uniqueConstraints = [UniqueConstraint(
        name = "uk_chave_pix",
        columnNames = ["chave"]
    )]
)
class ChavePix(

    @field:NotNull
    @Column(nullable = false)
    val clienteId: UUID,

    @field:NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val tipoDeChave: EnumTipoDeChave,

    @field:NotBlank
    @Column(nullable = false)
    var chave: String,

    @field:NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val tipoDeConta: EnumTipoDeConta,


    @field:Valid
    @Embedded
    val conta: ContaAssociada
) {

    @Id
    @GeneratedValue
    val id: UUID? = null

    @Column(nullable = false)
    val criadaEm: LocalDateTime = LocalDateTime.now()

    override fun toString(): String {
        return "ChavePix(clienteId=$clienteId, tipo=$tipoDeChave, chave='$chave', conta=$tipoDeConta, id=$id, criadaEm=$criadaEm)"
    }
}