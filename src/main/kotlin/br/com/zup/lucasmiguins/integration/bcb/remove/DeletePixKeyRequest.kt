package br.com.zup.lucasmiguins.integration.bcb.remove

import br.com.zup.lucasmiguins.pix.ContaAssociada

data class DeletePixKeyRequest(
    val key: String,
    val participant: String = ContaAssociada.ITAU_UNIBANCO_ISPB,
)