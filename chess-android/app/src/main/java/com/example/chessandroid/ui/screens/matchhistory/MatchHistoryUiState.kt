package com.example.chessandroid.ui.screens.matchhistory

/**
 * UI State for the Match History screen
 */
data class MatchHistoryUiState(
    val matches: List<ChessMatch> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String = "",
    val currentPage: Int = 0,
    val totalPages: Int = 0,
    val pageSize: Int = 10
)