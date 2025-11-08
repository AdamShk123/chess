package com.example.chessandroid.ui.screens.matchhistory

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chessandroid.data.repository.IMatchHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Match History screen
 */
@HiltViewModel
class MatchHistoryViewModel @Inject constructor(
    private val repository: IMatchHistoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MatchHistoryUiState())
    val uiState: StateFlow<MatchHistoryUiState> = _uiState.asStateFlow()

    init {
        loadMatchHistory()
    }

    /**
     * Loads a specific page of match history from the repository
     */
    private fun loadMatchHistory(
        pageNumber: Int = 0,
        pageSize: Int = 10,
        sortOrder: SortOrder = SortOrder.NEWEST_FIRST
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = "") }

            repository.getMatchHistory(pageNumber = pageNumber, pageSize = pageSize, sortOrder = sortOrder)
                .onSuccess { page ->
                    Log.d("MatchHistoryViewModel", "Loaded $page")
                    _uiState.update {
                        it.copy(
                            matches = page.matches,
                            currentPage = page.currentPage,
                            totalPages = page.totalPages,
                            pageSize = page.pageSize,
                            isLoading = false,
                            errorMessage = ""
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to load match history"
                        )
                    }
                }
        }
    }

    /**
     * Refreshes the current page of match history
     */
    fun refresh() {
        val state = _uiState.value
        loadMatchHistory(state.currentPage, state.pageSize, state.sortOrder)
    }

    /**
     * Navigates to the previous page
     */
    fun previousPage() {
        val state = _uiState.value
        loadMatchHistory(state.currentPage - 1, state.pageSize, state.sortOrder)
    }

    /**
     * Navigates to the next page
     */
    fun nextPage() {
        val state = _uiState.value
        loadMatchHistory(state.currentPage + 1, state.pageSize, state.sortOrder)
    }

    /**
     * Navigates to a specific page
     */
    fun selectPage(pageNumber: Int) {
        if (pageNumber != _uiState.value.currentPage) {
            val state = _uiState.value
            loadMatchHistory(pageNumber, state.pageSize, state.sortOrder)
        }
    }

    /**
     * Changes the page size and resets to first page
     * Updates UI state immediately (optimistic), then fetches data
     */
    fun changePageSize(newPageSize: Int) {
        _uiState.update {
            it.copy(
                pageSize = newPageSize,
                currentPage = 0
            )
        }
        loadMatchHistory(0, newPageSize, uiState.value.sortOrder)
    }

    /**
     * Changes the sort order and resets to first page
     * Updates UI state immediately (optimistic), then fetches data
     */
    fun changeSortOrder(newSortOrder: SortOrder) {
        _uiState.update {
            it.copy(
                sortOrder = newSortOrder,
                currentPage = 0
            )
        }
        val state = _uiState.value
        loadMatchHistory(0, state.pageSize, newSortOrder)
    }
}