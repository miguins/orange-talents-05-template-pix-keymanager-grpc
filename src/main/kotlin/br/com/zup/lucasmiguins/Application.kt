package br.com.zup.lucasmiguins

import io.micronaut.runtime.Micronaut.*
fun main(args: Array<String>) {
	build()
	    .args(*args)
		.packages("br.com.zup.lucasmiguins")
		.start()
}

