package br.com.zup.lucasmiguins.pix.registra

import br.com.zup.lucasmiguins.integration.bcb.BancoCentralClient
import br.com.zup.lucasmiguins.integration.bcb.registra.CreatePixKeyRequest
import br.com.zup.lucasmiguins.integration.itau.ContasClientesItauClient
import br.com.zup.lucasmiguins.pix.ChavePix
import br.com.zup.lucasmiguins.pix.exceptions.ChavePixExistenteException
import br.com.zup.lucasmiguins.pix.ChavePixRepository
import br.com.zup.lucasmiguins.pix.ContaAssociadaRepository
import io.micronaut.validation.Validated
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Validated
@Singleton
class NovaChavePixService(
    @Inject val chavePixRepository: ChavePixRepository,
    @Inject val contaAssociadaRepository: ContaAssociadaRepository,
    @Inject val itauClient: ContasClientesItauClient,
    @Inject val bcbClient: BancoCentralClient
) {

    @Transactional
    fun registra(@Valid novaChave: NovaChavePix): ChavePix {

        if (chavePixRepository.existsByChave(novaChave.chave))
            throw ChavePixExistenteException("Já existe um registro para a chave: '${novaChave.chave}'")

        val response = itauClient.buscaContaPorTipo(novaChave.clienteId!!, novaChave.tipoDeConta!!.name)
        val conta = response.body()?.toModel() ?: throw IllegalStateException("Cliente não encontrado no Itau")

        var chave = novaChave.toChavePix(conta)

        val possivelContaAssociada = contaAssociadaRepository.findByCpfDoTitular(cpfDoTitular = conta.cpfDoTitular)
        if (possivelContaAssociada.isPresent) {
            chave = novaChave.toChavePix(possivelContaAssociada.get())
        }

        val bcbResponse = bcbClient.criarChavePix(CreatePixKeyRequest.of(chave)).body()
            ?: throw IllegalStateException("Erro ao registrar chave Pix no Banco Central do Brasil (BCB)")
        chave.chave = bcbResponse.key

        chavePixRepository.save(chave)

        return chave
    }
}