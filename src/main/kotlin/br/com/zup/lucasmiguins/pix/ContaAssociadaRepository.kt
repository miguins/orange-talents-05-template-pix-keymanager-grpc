package br.com.zup.lucasmiguins.pix

import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface ContaAssociadaRepository : JpaRepository<ContaAssociada, Long>{

    fun findByCpfDoTitular(cpfDoTitular: String): Optional<ContaAssociada>
}