package com.example.chessandroid.data.repository

import com.example.chessandroid.ui.screens.matchhistory.ChessMatch
import com.example.chessandroid.ui.screens.matchhistory.MatchResult
import com.example.chessandroid.ui.screens.matchhistory.PlayerColor
import kotlinx.coroutines.delay
import java.time.LocalDateTime

/**
 * Repository for managing match history data
 * This will handle data fetching from remote/local sources
 */
class MatchHistoryRepository : IMatchHistoryRepository {

    /**
     * Fetches match history for the current user
     * TODO: Replace with actual API call or database query
     */
    override suspend fun getMatchHistory(): Result<List<ChessMatch>> {
        return try {
            // Simulate network delay
            delay(500)

            // For now, return mock data
            // In production, this would fetch from an API or local database
            val matches = listOf(
                ChessMatch(
                    id = "1",
                    opponent = "GrandMaster99",
                    result = MatchResult.WIN,
                    playerColor = PlayerColor.WHITE,
                    date = LocalDateTime.now().minusDays(1),
                    moves = 52,
                    duration = "18:45"
                ),
                ChessMatch(
                    id = "2",
                    opponent = "ChessNinja",
                    result = MatchResult.LOSS,
                    playerColor = PlayerColor.BLACK,
                    date = LocalDateTime.now().minusDays(2),
                    moves = 38,
                    duration = "12:20"
                ),
                ChessMatch(
                    id = "3",
                    opponent = "RookiePlayer",
                    result = MatchResult.DRAW,
                    playerColor = PlayerColor.WHITE,
                    date = LocalDateTime.now().minusDays(3),
                    moves = 67,
                    duration = "25:10"
                ),
                ChessMatch(
                    id = "4",
                    opponent = "KnightRider",
                    result = MatchResult.WIN,
                    playerColor = PlayerColor.BLACK,
                    date = LocalDateTime.now().minusDays(5),
                    moves = 41,
                    duration = "14:35"
                ),
                ChessMatch(
                    id = "5",
                    opponent = "QueenSlayer",
                    result = MatchResult.LOSS,
                    playerColor = PlayerColor.WHITE,
                    date = LocalDateTime.now().minusDays(7),
                    moves = 29,
                    duration = "09:15"
                ),
                ChessMatch(
                    id = "6",
                    opponent = "KingSlayer",
                    result = MatchResult.WIN,
                    playerColor = PlayerColor.WHITE,
                    date = LocalDateTime.now().minusDays(8),
                    moves = 29,
                    duration = "09:15"
                )
            )

            Result.success(matches)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}