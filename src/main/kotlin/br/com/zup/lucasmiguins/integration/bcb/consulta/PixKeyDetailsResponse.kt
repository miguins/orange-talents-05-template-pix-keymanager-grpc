package br.com.zup.lucasmiguins.integration.bcb.consulta

import br.com.zup.lucasmiguins.integration.bcb.registra.CreatePixKeyRequest
import br.com.zup.lucasmiguins.pix.ContaAssociada
import br.com.zup.lucasmiguins.pix.Instituicoes
import br.com.zup.lucasmiguins.pix.consulta.ChavePixInfo
import br.com.zup.lucasmiguins.pix.enums.EnumTipoDeConta
import java.time.LocalDateTime

data class PixKeyDetailsResponse (
    val keyType: CreatePixKeyRequest.PixKeyType,
    val key: String,
    val bankAccount: CreatePixKeyRequest.BankAccount,
    val owner: CreatePixKeyRequest.Owner,
    val createdAt: LocalDateTime
) {

    fun toModel(): ChavePixInfo {
        return ChavePixInfo(
            tipo = keyType.domainType!!,
            chave = this.key,
            tipoDeConta = when (this.bankAccount.accountType) {
                CreatePixKeyRequest.BankAccount.AccountType.CACC -> EnumTipoDeConta.CONTA_CORRENTE
                CreatePixKeyRequest.BankAccount.AccountType.SVGS -> EnumTipoDeConta.CONTA_POUPANCA
            },
            conta = ContaAssociada(
                instituicao = Instituicoes.nome(bankAccount.participant),
                nomeDoTitular = owner.name,
                cpfDoTitular = owner.taxIdNumber,
                agencia = bankAccount.branch,
                numeroDaConta = bankAccount.accountNumber
            )
        )
    }
}