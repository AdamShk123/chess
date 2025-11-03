package com.example.chessandroid.ui.screens.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.auth0.android.authentication.AuthenticationException
import com.example.chessandroid.data.auth.SignUpError
import com.example.chessandroid.data.repository.IUserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val userRepository: IUserRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, errorMessage = "") }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, errorMessage = "") }
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _uiState.update { it.copy(confirmPassword = confirmPassword, errorMessage = "") }
    }

    fun onSignUpClick() {
        val currentState = _uiState.value

        // Validation
        when {
            currentState.email.isBlank() -> {
                _uiState.update { it.copy(errorMessage = "Please enter your email") }
                return
            }
            !isValidEmail(currentState.email) -> {
                _uiState.update { it.copy(errorMessage = "Please enter a valid email") }
                return
            }
            currentState.password.isBlank() -> {
                _uiState.update { it.copy(errorMessage = "Please enter a password") }
                return
            }
            currentState.password.length < 8 -> {
                _uiState.update { it.copy(errorMessage = "Password must be at least 8 characters") }
                return
            }
            currentState.password != currentState.confirmPassword -> {
                _uiState.update { it.copy(errorMessage = "Passwords do not match") }
                return
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            userRepository.signUp(currentState.email, currentState.password)
                .onSuccess { _ ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isSignUpSuccessful = true,
                            errorMessage = ""
                        )
                    }
                }
                .onFailure { error ->
                    val errorMsg = SignUpError.getUserMessage(
                        code = (error as? AuthenticationException)?.getCode(),
                        defaultMessage = error.message ?: "Sign up failed"
                    )

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = errorMsg
                        )
                    }
                }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}