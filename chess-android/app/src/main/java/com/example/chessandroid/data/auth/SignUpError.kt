package com.example.chessandroid.data.auth

/**
 * Auth0 signup error codes
 * Reference: https://auth0.com/docs/libraries/common-auth0-library-authentication-errors
 */
enum class SignUpError(val code: String, val userMessage: String) {
    USER_EXISTS("user_exists", "This email is already registered. Please go back and log in instead."),
    USERNAME_EXISTS("username_exists", "This username is already taken. Please choose a different one."),
    INVALID_SIGNUP("invalid_signup", "Sign up failed. Please check your information."),
    INVALID_PASSWORD("invalid_password", "Password does not meet requirements."),
    PASSWORD_DICTIONARY_ERROR("password_dictionary_error", "This password is too common. Please choose a stronger password."),
    PASSWORD_NO_USER_INFO_ERROR("password_no_user_info_error", "Password cannot be based on your personal information."),
    PASSWORD_STRENGTH_ERROR("password_strength_error", "Password is not strong enough."),
    UNAUTHORIZED("unauthorized", "Sign up is not allowed.");

    companion object {
        fun fromCode(code: String?): SignUpError? {
            return entries.find { it.code == code }
        }

        fun getUserMessage(code: String?, defaultMessage: String = "Sign up failed"): String {
            return fromCode(code)?.userMessage ?: defaultMessage
        }
    }
}