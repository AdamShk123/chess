package com.example.chessandroid.ui.screens.matchhistory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.chessandroid.ui.theme.ChessAndroidTheme
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchHistoryScreen(
    viewModel: MatchHistoryViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    MatchHistoryContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onRefresh = { viewModel.refresh() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchHistoryContent(
    uiState: MatchHistoryUiState,
    onBackClick: () -> Unit = {},
    onRefresh: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Match History",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = uiState.isLoading,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.errorMessage.isNotEmpty() -> {
                    ErrorView(
                        message = uiState.errorMessage,
                        onRetry = onRefresh,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                uiState.matches.isEmpty() && !uiState.isLoading -> {
                    EmptyMatchHistoryView(
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.matches) { match ->
                            MatchHistoryItem(
                                match = match
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ErrorView(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Error",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

@Composable
fun EmptyMatchHistoryView(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No matches yet",
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Play your first game to see your match history",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun MatchHistoryItem(
    match: ChessMatch,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side: Match info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "vs ${match.opponent}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Color indicator
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(
                                color = if (match.playerColor == PlayerColor.WHITE) Color.White else Color.Black,
                                shape = RoundedCornerShape(4.dp)
                            )
                    )

                    Text(
                        text = match.playerColor.displayName,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${match.formattedDate} at ${match.formattedTime}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${match.moves} moves · ${match.duration}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Right side: Result badge
            ResultBadge(result = match.result)
        }
    }
}

@Composable
fun ResultBadge(
    result: MatchResult,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor) = when (result) {
        MatchResult.WIN -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
        MatchResult.LOSS -> MaterialTheme.colorScheme.error to MaterialTheme.colorScheme.onError
        MatchResult.DRAW -> MaterialTheme.colorScheme.tertiary to MaterialTheme.colorScheme.onTertiary
    }

    Surface(
        modifier = modifier,
        color = backgroundColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = result.displayName,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MatchHistoryScreenEmptyPreview() {
    ChessAndroidTheme {
        MatchHistoryContent(
            uiState = MatchHistoryUiState()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MatchHistoryScreenPreview() {
    ChessAndroidTheme {
        MatchHistoryContent(
            uiState = MatchHistoryUiState(matches = getSampleMatches())
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MatchHistoryItemPreview() {
    ChessAndroidTheme {
        MatchHistoryItem(
            match = ChessMatch(
                id = "1",
                opponent = "Player123",
                result = MatchResult.WIN,
                playerColor = PlayerColor.WHITE,
                date = LocalDateTime.now(),
                moves = 45,
                duration = "15:30"
            )
        )
    }
}

// Sample data for previews
fun getSampleMatches(): List<ChessMatch> {
    return listOf(
        ChessMatch(
            id = "1",
            opponent = "GrandMaster99",
            result = MatchResult.WIN,
            playerColor = PlayerColor.WHITE,
            date = LocalDateTime.now().minusDays(1),
            moves = 52,
            duration = "18:45"
        ),
        ChessMatch(
            id = "2",
            opponent = "ChessNinja",
            result = MatchResult.LOSS,
            playerColor = PlayerColor.BLACK,
            date = LocalDateTime.now().minusDays(2),
            moves = 38,
            duration = "12:20"
        ),
        ChessMatch(
            id = "3",
            opponent = "RookiePlayer",
            result = MatchResult.DRAW,
            playerColor = PlayerColor.WHITE,
            date = LocalDateTime.now().minusDays(3),
            moves = 67,
            duration = "25:10"
        ),
        ChessMatch(
            id = "4",
            opponent = "KnightRider",
            result = MatchResult.WIN,
            playerColor = PlayerColor.BLACK,
            date = LocalDateTime.now().minusDays(5),
            moves = 41,
            duration = "14:35"
        ),
        ChessMatch(
            id = "5",
            opponent = "QueenSlayer",
            result = MatchResult.LOSS,
            playerColor = PlayerColor.WHITE,
            date = LocalDateTime.now().minusDays(7),
            moves = 29,
            duration = "09:15"
        ),
        ChessMatch(
            id = "6",
            opponent = "KingSlayer",
            result = MatchResult.WIN,
            playerColor = PlayerColor.WHITE,
            date = LocalDateTime.now().minusDays(8),
            moves = 29,
            duration = "09:15"
        )
    )
}