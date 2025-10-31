package com.example.chess_backend.match

import com.example.chess_backend.user.User
import com.example.chess_backend.user.UserRepository
import com.fasterxml.jackson.databind.ObjectMapper
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
class
MatchControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var matchRepository: MatchRepository

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private lateinit var alice: User
    private lateinit var bob: User

    @BeforeEach
    fun setup() {
        // Clean database before each test
        matchRepository.deleteAll()
        userRepository.deleteAll()

        // Create test users
        alice = userRepository.save(User(name = "Alice", email = "alice@example.com", auth0Id = "auth0|alice123"))
        bob = userRepository.save(User(name = "Bob", email = "bob@example.com", auth0Id = "auth0|bob123"))
    }

    @Test
    fun `should create match successfully`() {
        val request = CreateMatchRequest(
            whitePlayerId = alice.id!!,
            blackPlayerId = bob.id!!
        )

        mockMvc.perform(
            post("/api/matches")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.whitePlayerId").value(alice.id))
            .andExpect(jsonPath("$.blackPlayerId").value(bob.id))
            .andExpect(jsonPath("$.status").value("PENDING"))
            .andExpect(jsonPath("$.result").value("ONGOING"))
    }

    @Test
    fun `should return 404 when creating match with invalid player`() {
        val request = CreateMatchRequest(
            whitePlayerId = 999,
            blackPlayerId = bob.id!!
        )

        mockMvc.perform(
            post("/api/matches")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.error").value("USER_NOT_FOUND"))
    }

    // Note: This test is disabled because GET /api/matches/me requires JWT authentication
    // In production, this endpoint is protected and requires a valid Auth0 token
    // TODO: Add test with mock JWT token when implementing full auth testing

    @Test
    fun `should update match status and result`() {
        val match = matchRepository.save(Match(
            whitePlayer = alice,
            blackPlayer = bob,
            status = MatchStatus.IN_PROGRESS,
            result = MatchResult.ONGOING
        ))

        val updateRequest = UpdateMatchRequest(
            status = MatchStatus.COMPLETED,
            result = MatchResult.WHITE_WIN
        )

        mockMvc.perform(
            put("/api/matches/${match.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("COMPLETED"))
            .andExpect(jsonPath("$.result").value("WHITE_WIN"))
    }

    @Test
    fun `should return 404 when updating non-existent match`() {
        val updateRequest = UpdateMatchRequest(
            status = MatchStatus.COMPLETED,
            result = MatchResult.WHITE_WIN
        )

        mockMvc.perform(
            put("/api/matches/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
        )
            .andExpect(status().isNotFound)
    }
}