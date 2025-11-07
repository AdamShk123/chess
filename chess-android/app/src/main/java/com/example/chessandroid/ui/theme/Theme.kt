package com.example.chessandroid.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Extended colors for Chess app
data class ChessExtendedColors(
    val winBackground: Color,
    val onWinBackground: Color,
    val drawBackground: Color,
    val onDrawBackground: Color,
    val loseBackground: Color,
    val onLoseBackground: Color
)

private val LocalChessExtendedColors = staticCompositionLocalOf {
    ChessExtendedColors(
        winBackground = ChessWinBackground,
        onWinBackground = ChessOnTertiary,
        drawBackground = ChessDrawBackground,
        onDrawBackground = ChessOnTertiary,
        loseBackground = ChessLoseBackground,
        onLoseBackground = ChessOnTertiary
    )
}

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
    onSurface = ChessOnSurface,
    error = ChessError
)

@Composable
fun ChessAndroidTheme(
    content: @Composable () -> Unit
) {
    val extendedColors = ChessExtendedColors(
        winBackground = ChessWinBackground,
        onWinBackground = ChessOnTertiary,
        drawBackground = ChessDrawBackground,
        onDrawBackground = ChessOnTertiary,
        loseBackground = ChessLoseBackground,
        onLoseBackground = ChessOnTertiary
    )

    CompositionLocalProvider(LocalChessExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = ChessColorScheme,
            typography = Typography,
            content = content
        )
    }
}

// Extension property to access extended colors
val MaterialTheme.chessColors: ChessExtendedColors
    @Composable
    get() = LocalChessExtendedColors.current