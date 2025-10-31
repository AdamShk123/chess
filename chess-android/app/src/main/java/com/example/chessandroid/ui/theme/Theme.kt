package com.example.chessandroid.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val ChessColorScheme = lightColorScheme(
    primary = ChessPrimary,
    secondary = ChessSecondary,
    tertiary = ChessTertiary,
    background = ChessBackground,
    surface = ChessSurface,
    onPrimary = ChessOnPrimary,
    onSecondary = ChessOnSecondary,
    onTertiary = ChessOnTertiary,
    onBackground = ChessOnBackground,
    onSurface = ChessOnSurface
)

@Composable
fun ChessAndroidTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ChessColorScheme,
        typography = Typography,
        content = content
    )
}