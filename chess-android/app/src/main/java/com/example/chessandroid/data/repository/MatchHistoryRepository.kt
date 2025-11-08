package com.example.chessandroid.data.repository

import com.example.chessandroid.data.api.ChessApiService
import com.example.chessandroid.data.api.mapper.toChessMatch
import com.example.chessandroid.ui.screens.matchhistory.SortOrder
import javax.inject.Inject

/**
 * Repository for managing match history data
 * Fetches match data from the backend API
 */
class MatchHistoryRepository @Inject constructor(
    private val apiService: ChessApiService,
    private val userRepository: IUserRepository
) : IMatchHistoryRepository {

    /**
     * Fetches a page of match history for the current user from the backend API
     * @param pageNumber Zero-indexed page number
     * @param pageSize Number of matches per page
     * @param sortOrder Sort order for matches (newest or oldest first)
     * @return Result containing MatchHistoryPage with matches and pagination metadata
     */
    override suspend fun getMatchHistory(
        pageNumber: Int,
        pageSize: Int,
        sortOrder: SortOrder
    ): Result<MatchHistoryPage> {
        return try {
            // Get access token for authentication
            val token = userRepository.getAccessToken().getOrThrow()
            val authHeader = "Bearer $token"

            // Get current user to determine player ID for mapping
            val currentUser = apiService.getCurrentUser(authHeader)

            // Convert sort order to API format
            val sortParam = when (sortOrder) {
                SortOrder.NEWEST_FIRST -> "createdAt,desc"
                SortOrder.OLDEST_FIRST -> "createdAt,asc"
            }

            // Fetch the requested page
            val pageResponse = apiService.getMyMatches(
                authorization = authHeader,
                pageNumber = pageNumber,
                pageSize = pageSize,
                sort = sortParam
            )

            // Map matches and create page with metadata
            val matches = pageResponse.content.map { it.toChessMatch(currentUser.id) }
            val matchHistoryPage = MatchHistoryPage(
                matches = matches,
                currentPage = pageResponse.number,
                totalPages = pageResponse.totalPages,
                totalElements = pageResponse.totalElements,
                pageSize = pageResponse.size,
                isFirst = pageResponse.first,
                isLast = pageResponse.last
            )

            Result.success(matchHistoryPage)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}