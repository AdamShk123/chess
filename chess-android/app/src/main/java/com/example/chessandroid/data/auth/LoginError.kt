package com.example.chessandroid.data.auth

/**
 * Auth0 login error codes
 * Reference: https://auth0.com/docs/libraries/common-auth0-library-authentication-errors
 */
enum class LoginError(val code: String, val userMessage: String) {
    INVALID_USER_PASSWORD("invalid_user_password", "Invalid email or password."),
    ACCESS_DENIED("access_denied", "Access denied."),
    TOO_MANY_ATTEMPTS("too_many_attempts", "Account temporarily blocked due to too many failed login attempts."),
    UNAUTHORIZED("unauthorized", "Invalid email or password."),
    PASSWORD_LEAKED("password_leaked", "This password has been compromised. Please use a different password.");

    companion object {
        fun fromCode(code: String?): LoginError? {
            return entries.find { it.code == code }
        }

        fun getUserMessage(code: String?, defaultMessage: String = "Login failed"): String {
            return fromCode(code)?.userMessage ?: defaultMessage
        }
    }
}