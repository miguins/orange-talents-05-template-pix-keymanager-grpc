package br.com.zup.lucasmiguins.pix.remove

import br.com.zup.lucasmiguins.pix.ChavePixRepository
import br.com.zup.lucasmiguins.pix.exceptions.ChavePixClienteNaoEncontradaException
import br.com.zup.lucasmiguins.shared.validation.ValidUUID
import io.micronaut.validation.Validated
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.constraints.NotBlank

@Validated
@Singleton
class RemoveChavePixService(
    @Inject val repository: ChavePixRepository,
) {

    @Transactional
    fun remove(
        @NotBlank @ValidUUID clienteId: String,
        @NotBlank @ValidUUID pixId: String
    ) {

        val uuidPixId = UUID.fromString(pixId)
        val uuidClienteId = UUID.fromString(clienteId)

        val chavePix = repository.findByIdAndClienteId(uuidPixId, uuidClienteId)
            .orElseThrow { ChavePixClienteNaoEncontradaException("Não foi encontrada uma chave pix para o cliente") }

        repository.delete(chavePix)
    }
}
