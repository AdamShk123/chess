package com.example.chessandroid.ui.screens.matchhistory

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
    private fun loadMatchHistory(pageNumber: Int = 0) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = "") }

            val pageSize = _uiState.value.pageSize
            repository.getMatchHistory(pageNumber = pageNumber, pageSize = pageSize)
                .onSuccess { page ->
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
        loadMatchHistory(_uiState.value.currentPage)
    }

    /**
     * Navigates to the previous page
     */
    fun previousPage() {
        val currentPage = _uiState.value.currentPage
        if (currentPage > 0) {
            loadMatchHistory(currentPage - 1)
        }
    }

    /**
     * Navigates to the next page
     */
    fun nextPage() {
        val currentPage = _uiState.value.currentPage
        val totalPages = _uiState.value.totalPages
        if (currentPage < totalPages - 1) {
            loadMatchHistory(currentPage + 1)
        }
    }

    /**
     * Navigates to a specific page
     */
    fun selectPage(pageNumber: Int) {
        if (pageNumber != _uiState.value.currentPage) {
            loadMatchHistory(pageNumber)
        }
    }
}