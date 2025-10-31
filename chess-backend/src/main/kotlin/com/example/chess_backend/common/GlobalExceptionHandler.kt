package com.example.chess_backend.common

import com.example.chess_backend.match.MatchNotFoundException
import com.example.chess_backend.user.DuplicateEmailException
import com.example.chess_backend.user.DuplicateNameException
import com.example.chess_backend.user.UserAlreadyRegisteredException
import com.example.chess_backend.user.UserNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateNameException::class)
    fun handleDuplicateName(ex: DuplicateNameException): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            error = ErrorCode.DUPLICATE_NAME,
            message = ex.message ?: "Name already exists"
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error)
    }

    @ExceptionHandler(DuplicateEmailException::class)
    fun handleDuplicateEmail(ex: DuplicateEmailException): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            error = ErrorCode.DUPLICATE_EMAIL,
            message = ex.message ?: "Email already exists"
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error)
    }

    @ExceptionHandler(UserAlreadyRegisteredException::class)
    fun handleUserAlreadyRegistered(ex: UserAlreadyRegisteredException): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            error = ErrorCode.USER_ALREADY_REGISTERED,
            message = ex.message ?: "User is already registered"
        )
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error)
    }

    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFound(ex: UserNotFoundException): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            error = ErrorCode.USER_NOT_FOUND,
            message = ex.message ?: "User not found"
        )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error)
    }

    @ExceptionHandler(MatchNotFoundException::class)
    fun handleMatchNotFound(ex: MatchNotFoundException): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            error = ErrorCode.MATCH_NOT_FOUND,
            message = ex.message ?: "Match not found"
        )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationErrors(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val errors = ex.bindingResult.fieldErrors.joinToString(", ") { "${it.field}: ${it.defaultMessage}" }
        val error = ErrorResponse(
            error = ErrorCode.VALIDATION_ERROR,
            message = errors
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error)
    }
}