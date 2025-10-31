package com.example.chess_backend.user

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class UserService(private val userRepository: UserRepository) {
    fun getUser(id: Int): User {
        return userRepository.findByIdOrNull(id)
            ?: throw UserNotFoundException(id)
    }

    fun updateUser(id: Int, name: String?, email: String?): User {
        val user = userRepository.findByIdOrNull(id)
            ?: throw UserNotFoundException(id)

        // Check if new username conflicts with another user
        if (name != null && name != user.name && userRepository.existsByName(name)) {
            throw DuplicateNameException(name)
        }
        // Check if new email conflicts with another user
        if (email != null && email != user.email && userRepository.existsByEmail(email)) {
            throw DuplicateEmailException(email)
        }

        val updatedUser = User(
            id = user.id,
            name = name ?: user.name,
            email = email ?: user.email,
            auth0Id = user.auth0Id,
            createdAt = user.createdAt
        )
        return userRepository.save(updatedUser)
    }

    fun deleteUser(id: Int) {
        if (!userRepository.existsById(id)) {
            throw UserNotFoundException(id)
        }
        userRepository.deleteById(id)
    }

    fun getAll(): List<User> = userRepository.findAll()
}