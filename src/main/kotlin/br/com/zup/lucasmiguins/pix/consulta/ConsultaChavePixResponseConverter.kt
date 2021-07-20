package br.com.zup.lucasmiguins.pix.consulta

import br.com.zup.lucasmiguins.grpc.ConsultaChavePixResponse
import br.com.zup.lucasmiguins.grpc.EnumTipoDeChave
import br.com.zup.lucasmiguins.grpc.EnumTipoDeConta
import com.google.protobuf.Timestamp
import java.time.ZoneId

class ConsultaChavePixResponseConverter {

    fun convert(chaveInfo: ChavePixInfo): ConsultaChavePixResponse {
        return ConsultaChavePixResponse.newBuilder()
            .setClienteId(chaveInfo.clienteId?.toString() ?: "") // Protobuf usa "" como default value para String
            .setPixId(chaveInfo.pixId?.toString() ?: "") // Protobuf usa "" como default value para String
            .setChave(ConsultaChavePixResponse.ChavePix
                .newBuilder()
                .setTipo(EnumTipoDeChave.valueOf(chaveInfo.tipo.name))
                .setChave(chaveInfo.chave)
                .setConta(ConsultaChavePixResponse.ChavePix.ContaInfo.newBuilder()
                    .setTipo(EnumTipoDeConta.valueOf(chaveInfo.tipoDeConta.name))
                    .setInstituicao(chaveInfo.conta.instituicao)
                    .setNomeDoTitular(chaveInfo.conta.nomeDoTitular)
                    .setCpfDoTitular(chaveInfo.conta.cpfDoTitular)
                    .setAgencia(chaveInfo.conta.agencia)
                    .setNumeroDaConta(chaveInfo.conta.numeroDaConta)
                    .build()
                )
                .setCriadaEm(chaveInfo.registradaEm.let {
                    val createdAt = it.atZone(ZoneId.of("UTC")).toInstant()
                    Timestamp.newBuilder()
                        .setSeconds(createdAt.epochSecond)
                        .setNanos(createdAt.nano)
                        .build()
                })
            )
            .build()
    }
}