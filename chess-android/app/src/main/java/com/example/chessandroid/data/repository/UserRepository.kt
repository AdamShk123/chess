package com.example.chessandroid.data.repository

import android.content.Context
import android.util.Log
import com.example.chessandroid.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationAPIClient
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.authentication.storage.CredentialsManagerException
import com.auth0.android.authentication.storage.SecureCredentialsManager
import com.auth0.android.authentication.storage.SharedPreferencesStorage
import com.auth0.android.result.Credentials
import com.auth0.android.callback.Callback
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.coroutines.resume

/**
 * Implementation of IUserRepository
 * Handles user data operations
 */
class UserRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : IUserRepository {

    private val account = Auth0(
        context.getString(R.string.com_auth0_client_id),
        context.getString(R.string.com_auth0_domain)
    )

    private val authClient = AuthenticationAPIClient(account)

    private val credentialsManager = SecureCredentialsManager(context, authClient, SharedPreferencesStorage(context))

    override suspend fun getCurrentUser(): Result<User> {
        return try {
            delay(300) // Simulate network call
            Result.success(
                User(
                    id = "user123",
                    email = "player@chess.com",
                    displayName = "ChessMaster",
                    rating = 1500
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateProfile(user: User): Result<Unit> {
        return try {
            delay(300)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            val credentials = authClient
                .login(email, password, "Username-Password-Authentication")
                .validateClaims()
                .await()

            credentialsManager.saveCredentials(credentials)
            Result.success(Unit)
        } catch (e: AuthenticationException) {
            Log.e("Auth0", "Login failed - Code: ${e.getCode()}, Description: ${e.getDescription()}", e)
            Result.failure(e)
        }
    }

    override suspend fun signUp(email: String, password: String): Result<Unit> {
        return try {
            authClient
                .signUp(email = email, password = password, connection = "Username-Password-Authentication")
                .await()

            // Don't save credentials - user must verify email first
            Log.d("Auth0", "Sign up successful, verification email sent to $email")
            Result.success(Unit)
        } catch (e: AuthenticationException) {
            Log.e("Auth0", "Sign up failed - Code: ${e.getCode()}, Description: ${e.getDescription()}, StatusCode: ${e.statusCode}", e)
            Result.failure(e)
        }
    }

    // TODO: Implement resendVerificationEmail via backend API
    // This requires calling the Auth0 Management API from the backend, not the mobile client
    // override suspend fun resendVerificationEmail(email: String): Result<Unit>

    override fun logout() {
        credentialsManager.clearCredentials()
    }

    override suspend fun getAccessToken(): Result<String> {
        return getValidCredentials().map { it.accessToken }
    }

    override suspend fun hasValidCredentials(): Boolean {
        return credentialsManager.hasValidCredentials()
    }

    /**
     * Private helper to get valid credentials from secure storage
     * Automatically refreshes expired tokens
     */
    private suspend fun getValidCredentials(): Result<Credentials> {
        return suspendCancellableCoroutine { continuation ->
            credentialsManager.getCredentials(object : Callback<Credentials, CredentialsManagerException> {
                override fun onSuccess(result: Credentials) {
                    continuation.resume(Result.success(result))
                }

                override fun onFailure(error: CredentialsManagerException) {
                    continuation.resume(Result.failure(error))
                }
            })
        }
    }
}