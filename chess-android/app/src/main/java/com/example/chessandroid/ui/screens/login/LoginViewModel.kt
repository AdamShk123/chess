package com.example.chessandroid.ui.screens.login

import android.content.Context
import android.util.Log
import androidx.credentials.CreatePasswordRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetPasswordOption
import androidx.credentials.PasswordCredential
import androidx.credentials.exceptions.CreateCredentialException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.auth0.android.authentication.AuthenticationException
import com.example.chessandroid.data.auth.LoginError
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
class LoginViewModel @Inject constructor(
    private val userRepository: IUserRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val credentialManager = CredentialManager.create(context)
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        checkForSavedCredentials()
    }

    private fun checkForSavedCredentials() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Step 1: Check Auth0 credentials first
            if (userRepository.hasValidCredentials()) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isLoggedIn = true
                    )
                }
                return@launch  // User still logged in - skip credential picker
            }

            // Step 2: No Auth0 credentials - try system credentials
            try {
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(GetPasswordOption())
                    .build()

                val result = credentialManager.getCredential(context, request)

                when (val credential = result.credential) {
                    is PasswordCredential -> {
                        // User selected saved password - auto-login
                        autoLogin(credential.id, credential.password)
                    }
                }
            } catch (e: NoCredentialException) {
                // No saved credentials - show manual login form
                Log.d("CredentialManager", "No saved credentials found")
                _uiState.update { it.copy(isLoading = false, showLoginForm = true) }
            } catch (e: GetCredentialException) {
                // Error retrieving credentials - show manual login form
                Log.e("CredentialManager", "Error getting credentials", e)
                _uiState.update { it.copy(isLoading = false, showLoginForm = true) }
            }
        }
    }

    private fun autoLogin(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            userRepository.login(email, password)
                .onSuccess {
                    Log.d("CredentialManager", "Auto-login successful")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoggedIn = true,
                            errorMessage = ""
                        )
                    }
                }
                .onFailure { error ->
                    // Saved password was incorrect - show manual form
                    Log.e("CredentialManager", "Auto-login failed", error)
                    val errorMsg = LoginError.getUserMessage(
                        code = (error as? AuthenticationException)?.getCode(),
                        defaultMessage = "Saved password is incorrect. Please log in again."
                    )

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            showLoginForm = true,
                            email = email,  // Pre-fill email
                            errorMessage = errorMsg
                        )
                    }
                }
        }
    }

    private suspend fun saveCredentialToSystem(activity: Context, email: String, password: String) {
        try {
            Log.d("CredentialManager", "Attempting to save credential for: $email")
            Log.d("CredentialManager", "Activity context: ${activity.javaClass.simpleName}")

            val request = CreatePasswordRequest(
                id = email,
                password = password
            )

            Log.d("CredentialManager", "Calling createCredential...")
            val result = credentialManager.createCredential(activity, request)

            Log.d("CredentialManager", "Password saved to system successfully!")
            Log.d("CredentialManager", "Result: ${result.javaClass.simpleName}")
        } catch (e: CreateCredentialException) {
            when {
                e.message?.contains("No create options available", ignoreCase = true) == true -> {
                    Log.w("CredentialManager", "No credential provider available on this device")
                    Log.w("CredentialManager", "This is normal on emulators without properly configured Google Play Services")
                    Log.w("CredentialManager", "Try: Settings → Google → Autofill → Enable Google Password Manager")
                }
                e.message?.contains("cancelled", ignoreCase = true) == true -> {
                    Log.d("CredentialManager", "User cancelled the save password prompt")
                }
                else -> {
                    Log.w("CredentialManager", "CreateCredentialException: ${e.javaClass.simpleName}")
                    Log.w("CredentialManager", "Message: ${e.message}")
                    Log.w("CredentialManager", "Full exception:", e)
                }
            }
        } catch (e: Exception) {
            Log.e("CredentialManager", "Unexpected error saving credential: ${e.message}", e)
        }
    }

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, errorMessage = "") }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, errorMessage = "") }
    }

    fun onLoginClick(activity: Context) {
        val currentState = _uiState.value

        if (currentState.email.isBlank() || currentState.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter email and password") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            userRepository.login(currentState.email, currentState.password)
                .onSuccess { _ ->
                    // Save to system AFTER successful Auth0 login
                    saveCredentialToSystem(activity, currentState.email, currentState.password)

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoggedIn = true,
                            errorMessage = ""
                        )
                    }
                }
                .onFailure { error ->
                    val errorMsg = LoginError.getUserMessage(
                        code = (error as? AuthenticationException)?.getCode(),
                        defaultMessage = error.message ?: "Login failed"
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

    fun onGoogleLogin() {
        // TODO: Implement Google Sign-In
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Placeholder implementation
            kotlinx.coroutines.delay(1000)

            _uiState.update {
                it.copy(
                    isLoading = false,
                    isLoggedIn = true
                )
            }
        }
    }
}