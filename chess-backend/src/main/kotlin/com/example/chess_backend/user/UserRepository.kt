package com.example.chess_backend.user

import org.springframework.data.repository.ListCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : ListCrudRepository<User, Int> {
    fun existsByName(name: String): Boolean
    fun existsByEmail(email: String): Boolean
    fun existsByAuth0Id(auth0Id: String): Boolean
    fun findByAuth0Id(auth0Id: String): User?
}