package com.example.chessandroid.ui.screens.signup

import android.content.Context
import android.util.Log
import androidx.credentials.CreatePasswordRequest
import androidx.credentials.CredentialManager
import androidx.credentials.exceptions.CreateCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.auth0.android.authentication.AuthenticationException
import com.example.chessandroid.data.auth.SignUpError
import com.example.chessandroid.data.repository.IUserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val userRepository: IUserRepository,
    @ApplicationContext private val context: Context
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

    fun onSignUpClick(activity: Context) {
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
                    // Save to system AFTER successful signup
                    saveCredentialToSystem(activity, currentState.email, currentState.password)

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isSignUpSuccessful = true,
                            errorMessage = ""
                        )
                    }
                }
                .onFailure { error ->
                    val errorMsg = if (error is AuthenticationException) {
                        val code = error.getCode()
                        val description = error.getDescription() ?: ""

                        // Check if it's a user already exists error
                        if (code == "invalid_signup" &&
                            (description.contains("user", ignoreCase = true) ||
                             description.contains("already", ignoreCase = true) ||
                             description.contains("exist", ignoreCase = true))) {
                            "This email is already registered. Please go back and log in instead."
                        } else {
                            SignUpError.getUserMessage(code, error.message ?: "Sign up failed")
                        }
                    } else {
                        error.message ?: "Sign up failed"
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = errorMsg
                        )
                    }
                }
        }
    }

    private suspend fun saveCredentialToSystem(activity: Context, email: String, password: String) {
        try {
            val credentialManager = CredentialManager.create(context)
            val request = CreatePasswordRequest(
                id = email,
                password = password
            )
            credentialManager.createCredential(activity, request)
            Log.d("CredentialManager", "Password saved to system after signup")
        } catch (e: CreateCredentialException) {
            // User declined to save - that's okay
            Log.d("CredentialManager", "User declined to save password")
        } catch (e: Exception) {
            Log.e("CredentialManager", "Error saving credential", e)
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}