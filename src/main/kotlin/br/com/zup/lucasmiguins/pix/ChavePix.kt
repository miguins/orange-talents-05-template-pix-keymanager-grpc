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
@Table(name = "chave_pix")
class ChavePix(

    @field:NotNull
    @Column( name = "cliente_id", nullable = false)
    val clienteId: UUID,

    @field:NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_chave", nullable = false)
    val tipoDeChave: EnumTipoDeChave,

    @field:NotBlank
    @Column(name= "chave", nullable = false)
    var chave: String,

    @field:NotNull
    @Enumerated(EnumType.STRING)
    @Column(name="tipo_conta", nullable = false)
    val tipoDeConta: EnumTipoDeConta,


    @field:Valid
    @ManyToOne(fetch = FetchType.EAGER, cascade = [CascadeType.PERSIST])
    @JoinColumn(name = "id_conta_associada", nullable = false)
    val conta: ContaAssociada
) {

    @Id
    @GeneratedValue
    @Column(name = "id_chave")
    val id: UUID? = null

    @Column(name = "data_criacao", nullable = false)
    val criadaEm: LocalDateTime = LocalDateTime.now()

    override fun toString(): String {
        return "ChavePix(clienteId=$clienteId, tipo=$tipoDeChave, chave='$chave', conta=$tipoDeConta, id=$id, criadaEm=$criadaEm)"
    }

    fun pertenceAo(clienteId: UUID) = this.clienteId == clienteId
}