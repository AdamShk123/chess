package com.example.chess_backend.user

import com.example.chess_backend.match.MatchRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@SpringBootTest
@AutoConfigureMockMvc
@org.springframework.test.context.ActiveProfiles("dev")
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

        mockMvc.perform(get("/api/users/${savedUser.id}"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(savedUser.id))
            .andExpect(jsonPath("$.name").value("Bob"))
            .andExpect(jsonPath("$.email").value("bob@example.com"))
            .andExpect(jsonPath("$.auth0Id").value("auth0|bob123"))
    }

    @Test
    fun `should return 404 when getting non-existent user`() {
        mockMvc.perform(get("/api/users/99999"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.error").value("USER_NOT_FOUND"))
            .andExpect(jsonPath("$.message").exists())
    }

    @Test
    fun `should update user successfully`() {
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
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(savedUser.id))
            .andExpect(jsonPath("$.name").value("Charles"))
            .andExpect(jsonPath("$.email").value("charles@example.com"))
    }

    @Test
    fun `should return 404 when updating non-existent user`() {
        val requestBody = """
            {
                "name": "New Name",
                "email": "new@example.com"
            }
        """.trimIndent()

        mockMvc.perform(
            put("/api/users/99999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.error").value("USER_NOT_FOUND"))
    }

    @Test
    fun `should delete user successfully`() {
        val user = User(name = "David", email = "david@example.com", auth0Id = "auth0|david123")
        val savedUser = userRepository.save(user)

        mockMvc.perform(delete("/api/users/${savedUser.id}"))
            .andExpect(status().isNoContent)
    }

    @Test
    fun `should return 404 when deleting non-existent user`() {
        mockMvc.perform(delete("/api/users/99999"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.error").value("USER_NOT_FOUND"))
    }
}