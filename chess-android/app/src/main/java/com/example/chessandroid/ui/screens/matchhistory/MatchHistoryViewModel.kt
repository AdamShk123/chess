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
     * Loads match history from the repository
     */
    fun loadMatchHistory() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = "") }

            repository.getMatchHistory()
                .onSuccess { matches ->
                    _uiState.update {
                        it.copy(
                            matches = matches,
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
     * Refreshes the match history
     */
    fun refresh() {
        loadMatchHistory()
    }
}