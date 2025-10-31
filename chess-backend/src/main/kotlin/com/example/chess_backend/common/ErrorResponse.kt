package com.example.chess_backend.common

data class ErrorResponse(
    val error: ErrorCode,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)