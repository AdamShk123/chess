package com.example.chessandroid.ui.screens.matchhistory

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Represents a single chess match in the history
 */
data class ChessMatch(
    val id: String,
    val opponent: String,
    val result: MatchResult,
    val playerColor: PlayerColor,
    val date: LocalDateTime
) {
    val formattedDate: String
        get() = date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))

    val formattedTime: String
        get() = date.format(DateTimeFormatter.ofPattern("HH:mm"))
}

/**
 * Possible match results
 */
enum class MatchResult(val displayName: String) {
    WIN("Win"),
    LOSS("Loss"),
    DRAW("Draw")
}

/**
 * Player color in the match
 */
enum class PlayerColor(val displayName: String) {
    WHITE("White"),
    BLACK("Black")
}