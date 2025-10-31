package com.example.chess_backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing


@SpringBootApplication
@EnableJpaAuditing
class ChessBackendApplication

fun main(args: Array<String>) {
	runApplication<ChessBackendApplication>(*args)
}
