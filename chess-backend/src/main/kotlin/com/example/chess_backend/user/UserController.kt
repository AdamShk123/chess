package com.example.chess_backend.user

import com.example.chess_backend.auth.AuthenticationService
import com.example.chess_backend.common.ForbiddenException
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*
import java.net.URI

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService,
    private val authenticationService: AuthenticationService
) {

    @PostMapping("/register")
    fun register(
        @AuthenticationPrincipal jwt: Jwt?,
        @Valid @RequestBody request: RegisterRequest
    ): ResponseEntity<UserResponse> {
        val user = authenticationService.registerUser(jwt, request.name)
        return ResponseEntity
            .created(URI.create("/api/users/${user.id}"))
            .body(user.toResponse())
    }

    @GetMapping("/me")
    fun getCurrentUser(@AuthenticationPrincipal jwt: Jwt?): ResponseEntity<UserResponse> {
        val user = authenticationService.getCurrentUser(jwt)
        return ResponseEntity.ok(user.toResponse())
    }

    @GetMapping("/{id}")
    fun getUser(@PathVariable id: Int): ResponseEntity<UserResponse> {
        val user = userService.getUser(id)
        return ResponseEntity.ok(user.toResponse())
    }

    @GetMapping
    fun getAllUsers(): ResponseEntity<List<UserResponse>> {
        val users = userService.getAll().map { it.toResponse() }
        return ResponseEntity.ok(users)
    }

    @PutMapping("/{id}")
    fun updateUser(
        @PathVariable id: Int,
        @Valid @RequestBody request: UpdateUserRequest,
        @AuthenticationPrincipal jwt: Jwt?
    ): ResponseEntity<UserResponse> {
        val currentUser = authenticationService.getCurrentUser(jwt)
        if (currentUser.id != id) {
            throw ForbiddenException("You can only update your own profile")
        }
        val user = userService.updateUser(id, request.name, request.email)
        return ResponseEntity.ok(user.toResponse())
    }

    @DeleteMapping("/{id}")
    fun deleteUser(
        @PathVariable id: Int,
        @AuthenticationPrincipal jwt: Jwt?
    ): ResponseEntity<Void> {
        val currentUser = authenticationService.getCurrentUser(jwt)
        if (currentUser.id != id) {
            throw ForbiddenException("You can only delete your own account")
        }
        userService.deleteUser(id)
        return ResponseEntity.noContent().build()
    }
}