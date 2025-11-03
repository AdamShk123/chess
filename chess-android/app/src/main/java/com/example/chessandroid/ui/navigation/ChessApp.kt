package com.example.chessandroid.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.chessandroid.ui.screens.home.HomeScreen
import com.example.chessandroid.ui.screens.leaderboard.LeaderboardScreen
import com.example.chessandroid.ui.screens.login.LoginScreen
import com.example.chessandroid.ui.screens.matchhistory.MatchHistoryScreen
import com.example.chessandroid.ui.screens.profile.ProfileScreen
import com.example.chessandroid.ui.screens.signup.SignUpScreen

@Composable
fun ChessApp() {
    val navController = rememberNavController()

    Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
        NavHost(
            navController = navController,
            startDestination = ChessRoute.Login.route
        ) {
            composable(ChessRoute.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(ChessRoute.Home.route) {
                            popUpTo(ChessRoute.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToSignUp = {
                        navController.navigate(ChessRoute.SignUp.route)
                    }
                )
            }

            composable(ChessRoute.SignUp.route) {
                SignUpScreen(
                    onSignUpSuccess = {
                        navController.navigate(ChessRoute.Home.route) {
                            popUpTo(ChessRoute.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onGoogleSignIn = {
                        // TODO: Implement Google Sign-In - same as LoginScreen
                        // When implemented, will navigate to Home on success
                        navController.navigate(ChessRoute.Home.route) {
                            popUpTo(ChessRoute.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(ChessRoute.Home.route) {
                HomeScreen(
                    onPlayOnline = {},
                    onPlayBot = {},
                    onMatchHistory = {
                        navController.navigate(ChessRoute.MatchHistory.route)
                    },
                    onLeaderboard = {
                        navController.navigate(ChessRoute.Leaderboard.route)
                    },
                    onProfileClick = {
                        navController.navigate(ChessRoute.Profile.route)
                    },
                    onLogOut = {
                        navController.navigate(ChessRoute.Login.route) {
                            popUpTo(ChessRoute.Home.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(ChessRoute.MatchHistory.route) {
                MatchHistoryScreen(
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            composable(ChessRoute.Profile.route) {
                ProfileScreen(
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            composable(ChessRoute.Leaderboard.route) {
                LeaderboardScreen(
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}