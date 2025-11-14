package com.example.chess_backend.match

import com.example.chess_backend.user.User
import com.example.chess_backend.user.UserNotFoundException
import com.example.chess_backend.user.UserRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest

class MatchServiceTest {

    private lateinit var matchRepository: MatchRepository
    private lateinit var userRepository: UserRepository
    private lateinit var matchService: MatchService

    @BeforeEach
    fun setup() {
        matchRepository = mockk()
        userRepository = mockk()
        matchService = MatchService(matchRepository, userRepository)
    }

    @Test
    fun `getMatchesByPlayer should return matches for player`() {
        // Given
        val whitePlayer = User(id = 1, name = "Alice", email = "alice@example.com", auth0Id = "auth0|alice123")
        val blackPlayer = User(id = 2, name = "Bob", email = "bob@example.com", auth0Id = "auth0|bob123")
        val matches = listOf(
            Match(id = 1, whitePlayer = whitePlayer, blackPlayer = blackPlayer),
            Match(id = 2, whitePlayer = blackPlayer, blackPlayer = whitePlayer)
        )
        val pageable = PageRequest.of(0, 10)
        val matchPage = PageImpl(matches, pageable, matches.size.toLong())

        every { userRepository.existsById(1) } returns true
        every { matchRepository.findByWhitePlayerIdOrBlackPlayerId(1, 1, pageable) } returns matchPage

        // When
        val result = matchService.getMatchesByPlayer(1, pageable)

        // Then
        assertNotNull(result)
        assertEquals(2, result.content.size)
        assertEquals(2, result.totalElements)
        verify { userRepository.existsById(1) }
        verify { matchRepository.findByWhitePlayerIdOrBlackPlayerId(1, 1, pageable) }
    }

    @Test
    fun `getMatchesByPlayer should throw UserNotFoundException when player does not exist`() {
        // Given
        val pageable = PageRequest.of(0, 10)
        every { userRepository.existsById(1) } returns false

        // When/Then
        assertThrows<UserNotFoundException> {
            matchService.getMatchesByPlayer(1, pageable)
        }
        verify { userRepository.existsById(1) }
        verify(exactly = 0) { matchRepository.findByWhitePlayerIdOrBlackPlayerId(any(), any(), any()) }
    }
}