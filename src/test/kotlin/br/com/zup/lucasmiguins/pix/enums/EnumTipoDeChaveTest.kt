package br.com.zup.lucasmiguins.pix.enums

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@MicronautTest
internal class EnumTipoDeChaveTest {

    @Nested
    inner class CPF {

        @Test
        internal fun `deve retornar false para um cpf invalido ou nao informado`() {
            with(EnumTipoDeChave.CPF) {
                assertFalse(valida(chave = "12345678"))
                assertFalse(valida(chave = ""))
                assertFalse(valida(chave = null))
            }
        }

        @Test
        internal fun `deve retornar true para um cpf valido`() {
            with(EnumTipoDeChave.CPF) {
                assertTrue(valida(chave = "42812084022"))
                assertTrue(valida(chave = "428.120.840-22"))
            }
        }
    }

    @Nested
    inner class CELULAR {
        @Test
        fun `deve retornar true para um celular valido`() {
            with(EnumTipoDeChave.CELULAR) {
                assertTrue(valida(chave = "+55 91 912345678".replace(" ", "")))
            }
        }

        @Test
        fun `deve retornar false para um celular invalido ou nao preenchido`() {
            with(EnumTipoDeChave.CELULAR) {
                assertFalse(valida(chave = "91912345678"))
                assertFalse(valida(chave = "+55abc1234"))
                assertFalse(valida(chave = ""))
                assertFalse(valida(chave = null))
            }
        }
    }

    @Nested
    inner class EMAIL {

        @Test
        fun `deve retornar true para um email valido`() {
            with(EnumTipoDeChave.EMAIL) {
                assertTrue(valida(chave = "email@valido.com.br"))
            }
        }

        @Test
        fun `deve retornar false para um email valido`() {
            with(EnumTipoDeChave.EMAIL) {
                assertTrue(valida(chave = "email@valido.com.br"))
            }
        }
    }

    @Nested
    inner class ALEATORIA {
        @Test
        fun `deve retornar true quando a chave for nula ou vazia`() {
            with(EnumTipoDeChave.ALEATORIA) {
                assertTrue(valida(chave = ""))
                assertTrue(valida(chave = null))
            }
        }

        @Test
        fun `deve retornar false quando a chave for preenchida`() {
            with(EnumTipoDeChave.ALEATORIA) {
                assertFalse(valida(chave = "valor"))
            }
        }
    }
}