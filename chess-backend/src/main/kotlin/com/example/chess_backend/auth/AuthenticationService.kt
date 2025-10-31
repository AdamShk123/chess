package com.example.chess_backend.auth

import com.example.chess_backend.user.*
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service

@Service
class AuthenticationService(
    private val userRepository: UserRepository
) {
    /**
     * Registers a new user from the JWT token.
     * This should be called explicitly when the user signs up.
     *
     * @param jwt The validated JWT token from Auth0 (null in dev mode)
     * @param customName Optional custom name (uses JWT claim if not provided)
     * @return The newly created User entity
     * @throws UserAlreadyRegisteredException if user already exists
     */
    fun registerUser(jwt: Jwt?, customName: String? = null): User {
        // Dev mode: create a test user
        if (jwt == null) {
            return createDefaultDevUser()
        }

        val auth0Id = jwt.subject

        // Check if user already exists
        if (userRepository.existsByAuth0Id(auth0Id)) {
            throw UserAlreadyRegisteredException(auth0Id)
        }

        return createUserFromJwt(jwt, customName)
    }

    /**
     * Gets the current authenticated user from the JWT token.
     * This is the key abstraction that decouples Auth0 from the rest of the application.
     *
     * Does NOT auto-create users - users must register first via /api/users/register.
     *
     * @param jwt The validated JWT token from Auth0 (null in dev mode)
     * @return The User entity from the database
     * @throws RuntimeException if user is not registered
     */
    fun getCurrentUser(jwt: Jwt?): User {
        // Dev mode: return a default test user when JWT is null
        if (jwt == null) {
            return getDevModeUser()
        }

        val auth0Id = jwt.subject // The "sub" claim contains the Auth0 user ID

        // Try to find existing user
        return userRepository.findByAuth0Id(auth0Id)
            ?: throw RuntimeException("User not registered. Please register at /api/users/register")
    }

    /**
     * Returns a default test user for development mode.
     * Uses the first user in the database or creates a default user.
     */
    private fun getDevModeUser(): User {
        return userRepository.findByAuth0Id("auth0|alice-demo")
            ?: userRepository.findAll().firstOrNull()
            ?: createDefaultDevUser()
    }

    /**
     * Creates a default dev user if none exists in the database.
     */
    private fun createDefaultDevUser(): User {
        val user = User(
            name = "Dev User",
            email = "dev@example.com",
            auth0Id = "dev|default-user"
        )
        return userRepository.save(user)
    }

    /**
     * Creates a new user from JWT claims.
     * Extracts name and email from the Auth0 token.
     *
     * @param jwt The JWT token with user claims
     * @param customName Optional custom name (overrides JWT claim if provided)
     * @throws IllegalArgumentException if email is missing from JWT
     * @throws DuplicateNameException if name already exists
     * @throws DuplicateEmailException if email already exists
     */
    private fun createUserFromJwt(jwt: Jwt, customName: String? = null): User {
        val auth0Id = jwt.subject

        // Email is required - throw if missing
        val email = jwt.getClaimAsString("email")
            ?: throw IllegalArgumentException("Email claim is required in JWT token. User may need to grant email permission.")

        // Use custom name if provided, otherwise try JWT claims
        val name = customName ?: jwt.getClaimAsString("name")
            ?: jwt.getClaimAsString("nickname")
            ?: jwt.getClaimAsString("given_name")
            ?: email.substringBefore("@") // Fallback to email username

        // Check for duplicate name
        if (userRepository.existsByName(name)) {
            throw DuplicateNameException(name)
        }

        // Check for duplicate email
        if (userRepository.existsByEmail(email)) {
            throw DuplicateEmailException(email)
        }

        val user = User(
            name = name,
            email = email,
            auth0Id = auth0Id
        )

        return userRepository.save(user)
    }
}