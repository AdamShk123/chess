package com.example.chessandroid.data.api.mapper

import com.example.chessandroid.data.api.dto.MatchResponseDto
import com.example.chessandroid.ui.screens.matchhistory.ChessMatch
import com.example.chessandroid.ui.screens.matchhistory.MatchResult
import com.example.chessandroid.ui.screens.matchhistory.PlayerColor
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Mapper to convert backend DTOs to domain models
 */

/**
 * Converts a MatchResponseDto from the backend to a ChessMatch domain model
 */
fun MatchResponseDto.toChessMatch(currentUserId: Int): ChessMatch {
    // Determine if current user was white or black
    val isWhitePlayer = whitePlayerId == currentUserId
    val playerColor = if (isWhitePlayer) PlayerColor.WHITE else PlayerColor.BLACK

    // Determine opponent name based on player color
    val opponent = if (isWhitePlayer) blackPlayerName else whitePlayerName

    // Map backend result to match result from current user's perspective
    val matchResult = when (result) {
        "WHITE_WIN" -> if (isWhitePlayer) MatchResult.WIN else MatchResult.LOSS
        "BLACK_WIN" -> if (isWhitePlayer) MatchResult.LOSS else MatchResult.WIN
        "DRAW" -> MatchResult.DRAW
        else -> MatchResult.DRAW // Default for ONGOING or unknown
    }

    // Parse ISO-8601 date
    val date = LocalDateTime.parse(createdAt, DateTimeFormatter.ISO_DATE_TIME)

    return ChessMatch(
        id = id.toString(),
        opponent = opponent,
        result = matchResult,
        playerColor = playerColor,
        date = date
    )
}