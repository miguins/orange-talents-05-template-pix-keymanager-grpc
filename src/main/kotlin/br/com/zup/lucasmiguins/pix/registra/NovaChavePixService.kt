package br.com.zup.lucasmiguins.pix.registra

import br.com.zup.lucasmiguins.integration.bcb.BancoCentralClient
import br.com.zup.lucasmiguins.integration.bcb.registra.CreatePixKeyRequest
import br.com.zup.lucasmiguins.integration.itau.ContasClientesItauClient
import br.com.zup.lucasmiguins.pix.ChavePix
import br.com.zup.lucasmiguins.pix.exceptions.ChavePixExistenteException
import br.com.zup.lucasmiguins.pix.ChavePixRepository
import io.micronaut.validation.Validated
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Validated
@Singleton
class NovaChavePixService(
    @Inject val repository: ChavePixRepository,
    @Inject val itauClient: ContasClientesItauClient,
    @Inject val bcbClient: BancoCentralClient
) {

    @Transactional
    fun registra(@Valid novaChave: NovaChavePix): ChavePix {

        if (repository.existsByChave(novaChave.chave))
            throw ChavePixExistenteException("Já existe um registro para a chave: '${novaChave.chave}'")

        val response = itauClient.buscaContaPorTipo(novaChave.clienteId!!, novaChave.tipoDeConta!!.name)
        val conta = response.body()?.toModel() ?: throw IllegalStateException("Cliente não encontrado no Itau")

        val chave = novaChave.toChavePix(conta)

        val bcbResponse = bcbClient.criarChavePix(CreatePixKeyRequest.of(chave)).body()
            ?: throw IllegalStateException("Erro ao registrar chave Pix no Banco Central do Brasil (BCB)")
        chave.chave = bcbResponse.key

        repository.save(chave)

        return chave
    }
}