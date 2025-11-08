package com.example.chessandroid.data.repository

import com.example.chessandroid.ui.screens.matchhistory.SortOrder

/**
 * Interface for match history repository
 * Defines the contract for fetching match history data
 */
interface IMatchHistoryRepository {
    /**
     * Fetches a page of match history for the current user
     * @param pageNumber Zero-indexed page number
     * @param pageSize Number of matches per page
     * @param sortOrder Sort order for matches (newest or oldest first)
     * @return Result containing MatchHistoryPage with matches and pagination metadata
     */
    suspend fun getMatchHistory(
        pageNumber: Int,
        pageSize: Int,
        sortOrder: SortOrder
    ): Result<MatchHistoryPage>
}