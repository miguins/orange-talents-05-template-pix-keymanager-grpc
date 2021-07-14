package br.com.zup.lucasmiguins.shared.handlers

import io.micronaut.aop.Around
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.CLASS

@MustBeDocumented
@Retention(RUNTIME)
@Target(CLASS)
@Around
annotation class ErrorAroundHandler
