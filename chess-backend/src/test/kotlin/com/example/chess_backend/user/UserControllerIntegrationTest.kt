package com.example.chess_backend.user

import com.example.chess_backend.match.MatchRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var matchRepository: MatchRepository

    @BeforeEach
    fun setup() {
        // Delete matches first due to foreign key constraints
        matchRepository.deleteAll()
        userRepository.deleteAll()
    }

    @AfterEach
    fun cleanup() {
        matchRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    fun `should get user by id successfully`() {
        val user = User(name = "Bob", email = "bob@example.com", auth0Id = "auth0|bob123")
        val savedUser = userRepository.save(user)

        mockMvc.perform(
            get("/api/users/${savedUser.id}")
                .with(jwt().jwt { it.subject("auth0|bob123") })
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(savedUser.id))
            .andExpect(jsonPath("$.name").value("Bob"))
            .andExpect(jsonPath("$.email").value("bob@example.com"))
            .andExpect(jsonPath("$.auth0Id").value("auth0|bob123"))
    }

    @Test
    fun `should return 404 when getting non-existent user`() {
        // Create a user to authenticate with
        val user = User(name = "User1", email = "user1@example.com", auth0Id = "auth0|user1")
        userRepository.save(user)

        mockMvc.perform(
            get("/api/users/99999")
                .with(jwt().jwt { it.subject("auth0|user1") })
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.error").value("USER_NOT_FOUND"))
            .andExpect(jsonPath("$.message").exists())
    }

    @Test
    fun `should update user successfully when updating own profile`() {
        val user = User(name = "Charlie", email = "charlie@example.com", auth0Id = "auth0|charlie123")
        val savedUser = userRepository.save(user)

        val requestBody = """
            {
                "name": "Charles",
                "email": "charles@example.com"
            }
        """.trimIndent()

        mockMvc.perform(
            put("/api/users/${savedUser.id}")
                .with(jwt().jwt { it.subject("auth0|charlie123") })
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(savedUser.id))
            .andExpect(jsonPath("$.name").value("Charles"))
            .andExpect(jsonPath("$.email").value("charles@example.com"))
    }

    @Test
    fun `should return 403 when trying to update another user's profile`() {
        // Create two users
        val user1 = User(name = "User1", email = "user1@example.com", auth0Id = "auth0|user1")
        userRepository.save(user1)

        val user2 = User(name = "User2", email = "user2@example.com", auth0Id = "auth0|user2")
        val savedUser2 = userRepository.save(user2)

        val requestBody = """
            {
                "name": "Hacked",
                "email": "hacked@example.com"
            }
        """.trimIndent()

        // User1 trying to update User2's profile
        mockMvc.perform(
            put("/api/users/${savedUser2.id}")
                .with(jwt().jwt { it.subject("auth0|user1") })
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.error").value("FORBIDDEN"))
            .andExpect(jsonPath("$.message").value("You can only update your own profile"))
    }

    @Test
    fun `should return 403 when trying to update non-existent user (not owner)`() {
        // Create a user to authenticate with
        val user = User(name = "User1", email = "user1@example.com", auth0Id = "auth0|user1")
        userRepository.save(user)

        val requestBody = """
            {
                "name": "New Name",
                "email": "new@example.com"
            }
        """.trimIndent()

        // User1 (ID != 99999) trying to update user 99999
        // Should return 403 to avoid leaking existence information
        mockMvc.perform(
            put("/api/users/99999")
                .with(jwt().jwt { it.subject("auth0|user1") })
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.error").value("FORBIDDEN"))
    }

    @Test
    fun `should delete user successfully when deleting own account`() {
        val user = User(name = "David", email = "david@example.com", auth0Id = "auth0|david123")
        val savedUser = userRepository.save(user)

        mockMvc.perform(
            delete("/api/users/${savedUser.id}")
                .with(jwt().jwt { it.subject("auth0|david123") })
        )
            .andExpect(status().isNoContent)
    }

    @Test
    fun `should return 403 when trying to delete another user's account`() {
        // Create two users
        val user1 = User(name = "User1", email = "user1@example.com", auth0Id = "auth0|user1")
        userRepository.save(user1)

        val user2 = User(name = "User2", email = "user2@example.com", auth0Id = "auth0|user2")
        val savedUser2 = userRepository.save(user2)

        // User1 trying to delete User2's account
        mockMvc.perform(
            delete("/api/users/${savedUser2.id}")
                .with(jwt().jwt { it.subject("auth0|user1") })
        )
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.error").value("FORBIDDEN"))
            .andExpect(jsonPath("$.message").value("You can only delete your own account"))
    }

    @Test
    fun `should return 403 when trying to delete non-existent user (not owner)`() {
        // Create a user to authenticate with
        val user = User(name = "User1", email = "user1@example.com", auth0Id = "auth0|user1")
        userRepository.save(user)

        // User1 (ID != 99999) trying to delete user 99999
        // Should return 403 to avoid leaking existence information
        mockMvc.perform(
            delete("/api/users/99999")
                .with(jwt().jwt { it.subject("auth0|user1") })
        )
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.error").value("FORBIDDEN"))
    }
}