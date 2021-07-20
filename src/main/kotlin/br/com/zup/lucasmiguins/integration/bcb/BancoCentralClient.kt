package br.com.zup.lucasmiguins.integration.bcb

import br.com.zup.lucasmiguins.integration.bcb.consulta.PixKeyDetailsResponse
import br.com.zup.lucasmiguins.integration.bcb.registra.CreatePixKeyRequest
import br.com.zup.lucasmiguins.integration.bcb.registra.CreatePixKeyResponse
import br.com.zup.lucasmiguins.integration.bcb.remove.DeletePixKeyRequest
import br.com.zup.lucasmiguins.integration.bcb.remove.DeletePixKeyResponse
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.http.client.annotation.Client

@Client("\${bcb.pix.url}")
interface BancoCentralClient {

    @Post("/api/v1/pix/keys", produces = [MediaType.APPLICATION_XML], consumes = [MediaType.APPLICATION_XML])
    fun criarChavePix(@Body createPixKeyRequest: CreatePixKeyRequest): HttpResponse<CreatePixKeyResponse>

    @Delete("/api/v1/pix/keys/{key}", produces = [MediaType.APPLICATION_XML], consumes = [MediaType.APPLICATION_XML])
    fun deletarChavePix(@PathVariable key: String, @Body request: DeletePixKeyRequest): HttpResponse<DeletePixKeyResponse>

    @Get("/api/v1/pix/keys/{key}",
        consumes = [MediaType.APPLICATION_XML])
    fun consultaPorChave(@PathVariable key: String): HttpResponse<PixKeyDetailsResponse>
}