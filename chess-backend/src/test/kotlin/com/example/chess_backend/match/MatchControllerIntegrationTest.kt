package com.example.chess_backend.match

import com.example.chess_backend.user.User
import com.example.chess_backend.user.UserRepository
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc

@SpringBootTest
@AutoConfigureMockMvc
@org.springframework.test.context.ActiveProfiles("dev")
class MatchControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var matchRepository: MatchRepository

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

    // Note: GET /api/matches/me requires JWT authentication
    // In production, this endpoint is protected and requires a valid Auth0 token
    // TODO: Add test with mock JWT token when implementing full auth testing
}