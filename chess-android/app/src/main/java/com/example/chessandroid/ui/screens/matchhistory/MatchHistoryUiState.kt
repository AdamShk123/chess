package com.example.chessandroid.ui.screens.matchhistory

/**
 * Sort order for match history by date
 */
enum class SortOrder(val displayName: String) {
    NEWEST_FIRST("Newest First"),
    OLDEST_FIRST("Oldest First")
}

/**
 * UI State for the Match History screen
 */
data class MatchHistoryUiState(
    val matches: List<ChessMatch> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String = "",
    val currentPage: Int = 0,
    val totalPages: Int = 0,
    val pageSize: Int = 10,
    val sortOrder: SortOrder = SortOrder.NEWEST_FIRST
)