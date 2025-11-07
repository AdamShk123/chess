package com.example.chessandroid.data.repository

import com.example.chessandroid.ui.screens.matchhistory.ChessMatch

/**
 * Paginated match history response containing matches and pagination metadata
 */
data class MatchHistoryPage(
    val matches: List<ChessMatch>,
    val currentPage: Int,
    val totalPages: Int,
    val totalElements: Long,
    val pageSize: Int,
    val isFirst: Boolean,
    val isLast: Boolean
)