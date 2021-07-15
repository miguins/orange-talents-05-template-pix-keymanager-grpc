package br.com.zup.lucasmiguins.integration.bcb.registra

import java.time.LocalDateTime

data class CreatePixKeyResponse(
    val keyType: CreatePixKeyRequest.PixKeyType,
    val key: String,
    val bankAccount: CreatePixKeyRequest.BankAccount,
    val owner: CreatePixKeyRequest.Owner,
    val createdAt: LocalDateTime
)