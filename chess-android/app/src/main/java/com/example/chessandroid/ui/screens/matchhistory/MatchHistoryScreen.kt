package com.example.chessandroid.ui.screens.matchhistory

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.chessandroid.ui.theme.ChessAndroidTheme
import com.example.chessandroid.ui.theme.chessColors
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
        onRefresh = { viewModel.refresh() },
        onPreviousPage = { viewModel.previousPage() },
        onNextPage = { viewModel.nextPage() },
        onSelectPage = { viewModel.selectPage(it) },
        onChangePageSize = { viewModel.changePageSize(it) },
        onChangeSortOrder = { viewModel.changeSortOrder(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchHistoryContent(
    uiState: MatchHistoryUiState,
    onBackClick: () -> Unit = {},
    onRefresh: () -> Unit = {},
    onPreviousPage: () -> Unit = {},
    onNextPage: () -> Unit = {},
    onSelectPage: (Int) -> Unit = {},
    onChangePageSize: (Int) -> Unit = {},
    onChangeSortOrder: (SortOrder) -> Unit = {}
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
        },
        bottomBar = {
            if (uiState.matches.isNotEmpty() && uiState.totalPages > 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onPreviousPage,
                        enabled = uiState.currentPage != 0,
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous Page"
                        )
                    }
                    LazyRow(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items((0..<uiState.totalPages).toList()) { page ->
                            val isCurrentPage = page == uiState.currentPage
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        color = if (isCurrentPage) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            Color.Transparent
                                        }
                                    )
                            ) {
                                TextButton(
                                    onClick = { if (!isCurrentPage) onSelectPage(page) },
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = if (isCurrentPage) {
                                            MaterialTheme.colorScheme.onPrimary
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        }
                                    ),
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text(
                                        text = "${page + 1}",
                                        fontWeight = if (isCurrentPage) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                    IconButton(
                        onClick = onNextPage,
                        enabled = uiState.currentPage != uiState.totalPages - 1,
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next Page"
                        )
                    }
                }
            }
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
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Filter bar
                        FilterBar(
                            pageSize = uiState.pageSize,
                            sortOrder = uiState.sortOrder,
                            onChangePageSize = onChangePageSize,
                            onChangeSortOrder = onChangeSortOrder
                        )

                        // Match list
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
        shape = RoundedCornerShape(12.dp),
        colors = CardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            disabledContainerColor = MaterialTheme.colorScheme.surface,
            disabledContentColor = MaterialTheme.colorScheme.surface
        )
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
                    Surface(
                        modifier = Modifier.size(16.dp),
                        color = if (match.playerColor == PlayerColor.WHITE) Color.White else Color.Black,
                        shape = RoundedCornerShape(4.dp),
                        shadowElevation = 4.dp
                    ) {}

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
            }

            // Right side: Result badge
            ResultBadge(result = match.result)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBar(
    pageSize: Int,
    sortOrder: SortOrder,
    onChangePageSize: (Int) -> Unit,
    onChangeSortOrder: (SortOrder) -> Unit,
    modifier: Modifier = Modifier
) {
    var showPageSizeMenu by remember { mutableStateOf(false) }
    val pageSizeOptions = listOf(5, 10, 25, 50)

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Page size dropdown
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Per page:",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Box {
                    OutlinedButton(
                        onClick = { showPageSizeMenu = true },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(text = "$pageSize", fontSize = 14.sp)
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Select page size",
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showPageSizeMenu,
                        onDismissRequest = { showPageSizeMenu = false }
                    ) {
                        pageSizeOptions.forEach { size ->
                            DropdownMenuItem(
                                text = { Text("$size") },
                                onClick = {
                                    onChangePageSize(size)
                                    showPageSizeMenu = false
                                },
                                leadingIcon = if (size == pageSize) {
                                    { Icon(Icons.Default.Check, contentDescription = null) }
                                } else null
                            )
                        }
                    }
                }
            }

            // Sort toggle
            FilterChip(
                selected = sortOrder == SortOrder.NEWEST_FIRST,
                onClick = {
                    onChangeSortOrder(
                        if (sortOrder == SortOrder.NEWEST_FIRST) SortOrder.OLDEST_FIRST
                        else SortOrder.NEWEST_FIRST
                    )
                },
                label = { Text(sortOrder.displayName, fontSize = 14.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = if (sortOrder == SortOrder.NEWEST_FIRST)
                            Icons.Default.ArrowDownward
                        else
                            Icons.Default.ArrowUpward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                colors = FilterChipDefaults
                    .filterChipColors()
                    .copy(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    }
}

@Composable
fun ResultBadge(
    result: MatchResult,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor) = when (result) {
        MatchResult.WIN -> MaterialTheme.chessColors.winBackground to MaterialTheme.chessColors.onWinBackground
        MatchResult.LOSS -> MaterialTheme.chessColors.loseBackground to MaterialTheme.chessColors.onLoseBackground
        MatchResult.DRAW -> MaterialTheme.chessColors.drawBackground to MaterialTheme.chessColors.onDrawBackground
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
            uiState = MatchHistoryUiState(
                matches = getSampleMatches(),
                totalPages = 2),
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
                date = LocalDateTime.now()
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
            date = LocalDateTime.now().minusDays(1)
        ),
        ChessMatch(
            id = "2",
            opponent = "ChessNinja",
            result = MatchResult.LOSS,
            playerColor = PlayerColor.BLACK,
            date = LocalDateTime.now().minusDays(2)
        ),
        ChessMatch(
            id = "3",
            opponent = "RookiePlayer",
            result = MatchResult.DRAW,
            playerColor = PlayerColor.WHITE,
            date = LocalDateTime.now().minusDays(3)
        ),
        ChessMatch(
            id = "4",
            opponent = "KnightRider",
            result = MatchResult.WIN,
            playerColor = PlayerColor.BLACK,
            date = LocalDateTime.now().minusDays(5)
        )
    )
}