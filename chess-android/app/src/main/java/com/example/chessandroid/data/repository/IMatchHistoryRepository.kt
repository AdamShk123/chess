package com.example.chessandroid.data.repository

import com.example.chessandroid.ui.screens.matchhistory.ChessMatch

/**
 * Interface for match history repository
 * Defines the contract for fetching match history data
 */
interface IMatchHistoryRepository {
    /**
     * Fetches match history for the current user
     */
    suspend fun getMatchHistory(): Result<List<ChessMatch>>
}