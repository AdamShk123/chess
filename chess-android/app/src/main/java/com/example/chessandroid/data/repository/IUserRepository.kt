package com.example.chessandroid.data.repository

/**
 * Interface for user repository
 * Handles user profile and authentication data
 */
interface IUserRepository {
    /**
     * Get the current user's profile
     */
    suspend fun getCurrentUser(): Result<User>

    /**
     * Update user profile
     */
    suspend fun updateProfile(user: User): Result<Unit>

    /**
     * Login with credentials
     * Returns Unit on success, or failure with error message
     */
    suspend fun login(email: String, password: String): Result<Unit>

    /**
     * Sign up a new user with email and password
     * Returns Unit on success, or failure with error message
     */
    suspend fun signUp(email: String, password: String): Result<Unit>

    /**
     * Login with Google ID token
     * @param idToken The Google ID token obtained from Credential Manager
     * @return Result with Unit on success, or failure with error message
     */
    suspend fun loginWithGoogle(idToken: String): Result<Unit>

    // TODO: Implement resendVerificationEmail via backend API
    // Auth0's Management API is required to resend verification emails, which should be called
    // from the backend for security. The backend endpoint should:
    // 1. Accept user email
    // 2. Look up user ID from Auth0
    // 3. Call Management API: POST /api/v2/jobs/verification-email
    // suspend fun resendVerificationEmail(email: String): Result<Unit>

    /**
     * Logout the current user
     */
    fun logout()

    /**
     * Get the current access token for API calls
     * Automatically refreshes the token if expired
     * @return Result with access token string or failure
     */
    suspend fun getAccessToken(): Result<String>

    /**
     * Check if user has valid credentials stored
     * @return true if valid credentials exist, false otherwise
     */
    suspend fun hasValidCredentials(): Boolean
}

data class User(
    val id: String,
    val email: String,
    val displayName: String,
    val rating: Int = 1200
)