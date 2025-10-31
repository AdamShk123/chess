package com.example.chessandroid.data.repository

import android.content.Context
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
        return suspendCancellableCoroutine { continuation ->
            authClient.login(usernameOrEmail = email, password = password)
                .validateClaims()
                .start(object: Callback<Credentials, AuthenticationException> {
                    override fun onFailure(error: AuthenticationException) {
                        continuation.resume(Result.failure(error))
                    }

                    override fun onSuccess(result: Credentials) {
                        credentialsManager.saveCredentials(result)
                        continuation.resume(Result.success(Unit))
                    }
                })
        }
    }

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