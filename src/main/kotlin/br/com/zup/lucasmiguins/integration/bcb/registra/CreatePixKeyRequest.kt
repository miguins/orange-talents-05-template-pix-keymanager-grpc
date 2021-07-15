package br.com.zup.lucasmiguins.integration.bcb.registra

import br.com.zup.lucasmiguins.pix.ChavePix
import br.com.zup.lucasmiguins.pix.ContaAssociada
import br.com.zup.lucasmiguins.pix.enums.EnumTipoDeChave
import br.com.zup.lucasmiguins.pix.enums.EnumTipoDeConta

data class CreatePixKeyRequest(

    val keyType: PixKeyType,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner
) {
    companion object {

        fun of(chave: ChavePix): CreatePixKeyRequest {
            return CreatePixKeyRequest(
                keyType = PixKeyType.by(chave.tipoDeChave),
                key = chave.chave,
                bankAccount = BankAccount(
                    participant = ContaAssociada.ITAU_UNIBANCO_ISPB,
                    branch = chave.conta.agencia,
                    accountNumber = chave.conta.numeroDaConta,
                    accountType = BankAccount.AccountType.by(chave.tipoDeConta),
                ),
                owner = Owner(
                    type = Owner.OwnerType.NATURAL_PERSON,
                    name = chave.conta.nomeDoTitular,
                    taxIdNumber = chave.conta.cpfDoTitular
                )
            )
        }
    }

    data class Owner(
        val type: OwnerType,
        val name: String,
        val taxIdNumber: String
    ) {

        enum class OwnerType {
            NATURAL_PERSON,
            LEGAL_PERSON
        }
    }

    data class BankAccount(
        val participant: String,
        val branch: String,
        val accountNumber: String,
        val accountType: AccountType
    ) {

        enum class AccountType() {
            CACC,
            SVGS;

            companion object {
                fun by(domainType: EnumTipoDeConta): AccountType {
                    return when (domainType) {
                        EnumTipoDeConta.CONTA_CORRENTE -> CACC
                        EnumTipoDeConta.CONTA_POUPANCA -> SVGS
                    }
                }
            }
        }

    }

    enum class PixKeyType(val domainType: EnumTipoDeChave?) {

        CPF(EnumTipoDeChave.CPF),
        CNPJ(null),
        PHONE(EnumTipoDeChave.CELULAR),
        EMAIL(EnumTipoDeChave.EMAIL),
        RANDOM(EnumTipoDeChave.ALEATORIA);

        companion object {
            private val mapping = values().associateBy(PixKeyType::domainType)
            fun by(domainType: EnumTipoDeChave): PixKeyType {
                return mapping[domainType]
                    ?: throw IllegalArgumentException("PixKeyType invalid or not found for $domainType")
            }
        }
    }
}