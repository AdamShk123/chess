package com.example.chessandroid.ui.navigation

sealed class ChessRoute(val route: String) {
    data object Login : ChessRoute("login")
    data object Home : ChessRoute("home")
    data object MatchHistory : ChessRoute("match_history")
    data object Profile : ChessRoute("profile")
    data object Leaderboard : ChessRoute("leaderboard")
}