package com.example.chess_backend.user

import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertThrows
import org.springframework.data.repository.findByIdOrNull
import kotlin.test.assertEquals

class UserServiceTest {
    private lateinit var userRepository: UserRepository
    private lateinit var userService: UserService

    @BeforeEach
    fun setup() {
        userRepository = mockk()
        userService = UserService(userRepository)
    }

    @Test
    fun `given non existent user id, throw UserNotFoundException`() {
        every { userRepository.findByIdOrNull(1) } returns null

        assertThrows<UserNotFoundException> {
            userService.getUser(1)
        }
    }

    @Test
    fun `given existent user id, return user`() {
        val expected = User(
            id = 1,
            name = "Bob",
            email = "bob@gmail.com",
            auth0Id = "auth0|123"
        )

        every { userRepository.findByIdOrNull(1) } returns expected

        val result = userService.getUser(1)

        assertEquals(result, expected)
    }

    @Test
    fun `update user successfully with new name and email`() {
        val existingUser = User(
            id = 1,
            name = "Bob",
            email = "bob@gmail.com",
            auth0Id = "auth0|123"
        )
        val updatedUser = User(
            id = 1,
            name = "Alice",
            email = "alice@gmail.com",
            auth0Id = "auth0|123"
        )

        every { userRepository.findByIdOrNull(1) } returns existingUser
        every { userRepository.existsByName("Alice") } returns false
        every { userRepository.existsByEmail("alice@gmail.com") } returns false
        every { userRepository.save(any()) } returns updatedUser

        val result = userService.updateUser(1, "Alice", "alice@gmail.com")

        assertNotNull(result)
        assertEquals("Alice", result.name)
        assertEquals("alice@gmail.com", result.email)
    }

    @Test
    fun `fail to update user with duplicate name`() {
        val existingUser = User(
            id = 1,
            name = "Bob",
            email = "bob@gmail.com",
            auth0Id = "auth0|123"
        )

        every { userRepository.findByIdOrNull(1) } returns existingUser
        every { userRepository.existsByName("Alice") } returns true

        assertThrows<DuplicateNameException> {
            userService.updateUser(1, "Alice", null)
        }
    }

    @Test
    fun `fail to update user with duplicate email`() {
        val existingUser = User(
            id = 1,
            name = "Bob",
            email = "bob@gmail.com",
            auth0Id = "auth0|123"
        )

        every { userRepository.findByIdOrNull(1) } returns existingUser
        every { userRepository.existsByName("Bob") } returns false
        every { userRepository.existsByEmail("alice@gmail.com") } returns true

        assertThrows<DuplicateEmailException> {
            userService.updateUser(1, null, "alice@gmail.com")
        }
    }

    @Test
    fun `delete user successfully`() {
        every { userRepository.existsById(1) } returns true
        justRun { userRepository.deleteById(1) }

        userService.deleteUser(1)

        verify { userRepository.deleteById(1) }
    }

    @Test
    fun `fail to delete non-existent user`() {
        every { userRepository.existsById(1) } returns false

        assertThrows<UserNotFoundException> {
            userService.deleteUser(1)
        }
    }
}