package com.example.chessandroid.ui.screens.signup

data class SignUpUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String = "",
    val isSignUpSuccessful: Boolean = false
)