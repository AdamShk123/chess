package com.example.chessandroid.ui.screens.login

import android.content.Context
import android.util.Log
import androidx.credentials.CreatePasswordRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
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
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userRepository: IUserRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val credentialManager = CredentialManager.create(context)
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    // Store current nonce for Google Sign-In verification
    private var currentNonce: String? = null

    init {
        checkForSavedCredentials()
    }

    /**
     * Nonce utility functions for Google Sign-In security.
     *
     * SECURITY NOTE: The nonce prevents replay attacks by ensuring each
     * Google ID token is unique. Auth0's loginWithNativeSocialToken()
     * is assumed to validate nonces server-side, but this is not explicitly
     * documented. For production apps with high security requirements,
     * consider implementing backend nonce tracking or confirming with Auth0
     * that they maintain a "seen nonces" database.
     *
     * See: docs/google-login-implementation-plan.md for detailed security analysis
     *
     * References:
     * - Google: https://developer.android.com/identity/sign-in/credential-manager-siwg
     * - Auth0: https://auth0.com/docs/authenticate/identity-providers/social-identity-providers/google-native
     */
    private fun generateNonce(): String {
        val bytes = ByteArray(32)
        SecureRandom().nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    private fun hashNonce(nonce: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(nonce.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
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
                // Generate nonce for auto-login with Google if available
                val autoLoginNonce = generateNonce()
                val hashedAutoLoginNonce = hashNonce(autoLoginNonce)

                val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(true)
                    .setServerClientId(context.getString(com.example.chessandroid.R.string.google_web_client_id))
                    .setAutoSelectEnabled(false)
                    .setNonce(hashedAutoLoginNonce)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(GetPasswordOption())
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(context, request)

                when (val credential = result.credential) {
                    is PasswordCredential -> {
                        // User selected saved password - auto-login
                        autoLogin(credential.id, credential.password)
                    }
                    is CustomCredential -> {
                        // Google credential comes as CustomCredential
                        if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                            try {
                                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                                val idToken = googleIdTokenCredential.idToken

                                Log.d("CredentialManager", "Auto-login with Google")
                                userRepository.loginWithGoogle(idToken)
                                    .onSuccess {
                                        _uiState.update {
                                            it.copy(
                                                isLoading = false,
                                                isLoggedIn = true
                                            )
                                        }
                                    }
                                    .onFailure { error ->
                                        Log.e("CredentialManager", "Google auto-login failed", error)
                                        _uiState.update {
                                            it.copy(
                                                isLoading = false,
                                                showLoginForm = true,
                                                errorMessage = "Google sign-in failed. Please try again."
                                            )
                                        }
                                    }
                            } catch (e: Exception) {
                                Log.e("CredentialManager", "Failed to parse Google credential", e)
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        showLoginForm = true,
                                        errorMessage = "Failed to process Google credential."
                                    )
                                }
                            }
                        } else {
                            Log.w("CredentialManager", "Unknown custom credential type: ${credential.type}")
                            _uiState.update { it.copy(isLoading = false, showLoginForm = true) }
                        }
                    }
                }
            } catch (_: NoCredentialException) {
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

    fun onGoogleLogin(activity: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // Step 1: Generate nonce for security
                currentNonce = generateNonce()
                val hashedNonce = hashNonce(currentNonce!!)

                Log.d("GoogleSignIn", "Generated nonce, requesting Google ID token")

                // Step 2: Build Google ID option with nonce
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(context.getString(com.example.chessandroid.R.string.google_web_client_id))
                    .setNonce(hashedNonce)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                // Step 3: Get Google ID token via Credential Manager
                val result = credentialManager.getCredential(activity, request)
                val credential = result.credential

                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken

                Log.d("GoogleSignIn", "Google ID Token obtained, exchanging with Auth0")

                // Step 4: Login with Google via repository
                userRepository.loginWithGoogle(idToken)
                    .onSuccess {
                        Log.d("GoogleSignIn", "Google login successful")
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isLoggedIn = true,
                                errorMessage = ""
                            )
                        }
                    }
                    .onFailure { error ->
                        Log.e("GoogleSignIn", "Google login failed", error)
                        val errorMsg = LoginError.getUserMessage(
                            code = (error as? AuthenticationException)?.getCode(),
                            defaultMessage = error.message ?: "Google sign-in failed"
                        )

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = errorMsg
                            )
                        }
                    }
            } catch (e: GetCredentialException) {
                Log.e("GoogleSignIn", "Failed to get Google credential", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = when {
                            e.message?.contains("cancelled", ignoreCase = true) == true ->
                                "Google sign-in cancelled"
                            else -> "Google sign-in failed: ${e.message}"
                        }
                    )
                }
            } catch (e: Exception) {
                Log.e("GoogleSignIn", "Unexpected error during Google sign-in", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Google sign-in failed: ${e.message}"
                    )
                }
            }
        }
    }
}