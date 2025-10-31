package com.example.chess_backend.match

import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime

data class CreateMatchRequest(
    @field:NotNull(message = "White player ID is required")
    val whitePlayerId: Int,

    @field:NotNull(message = "Black player ID is required")
    val blackPlayerId: Int
)

data class UpdateMatchRequest(
    val status: MatchStatus?,
    val result: MatchResult?
)

data class MatchResponse(
    val id: Int,
    val whitePlayerId: Int,
    val whitePlayerName: String,
    val blackPlayerId: Int,
    val blackPlayerName: String,
    val status: MatchStatus,
    val result: MatchResult,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

fun Match.toResponse() = MatchResponse(
    id = id ?: 0,
    whitePlayerId = whitePlayer.id ?: 0,
    whitePlayerName = whitePlayer.name,
    blackPlayerId = blackPlayer.id ?: 0,
    blackPlayerName = blackPlayer.name,
    status = status,
    result = result,
    createdAt = createdAt,
    updatedAt = updatedAt
)