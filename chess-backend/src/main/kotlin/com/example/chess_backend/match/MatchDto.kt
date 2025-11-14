package com.example.chess_backend.match

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

data class MatchResponse(
    val id: Int,
    val whitePlayerId: Int,
    val whitePlayerName: String,
    val blackPlayerId: Int,
    val blackPlayerName: String,
    val status: MatchStatus,
    val result: MatchResult,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    @get:JsonProperty("isPlayerWhite")
    val isPlayerWhite: Boolean
)

fun Match.toResponse(userID: Int) = MatchResponse(
    id = id ?: 0,
    whitePlayerId = whitePlayer.id ?: 0,
    whitePlayerName = whitePlayer.name,
    blackPlayerId = blackPlayer.id ?: 0,
    blackPlayerName = blackPlayer.name,
    status = status,
    result = result,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isPlayerWhite = whitePlayer.id == userID
)