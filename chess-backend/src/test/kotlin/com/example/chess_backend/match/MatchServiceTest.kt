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
import org.springframework.data.repository.findByIdOrNull

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
    fun `createMatch should create match when both players exist`() {
        // Given
        val whitePlayer = User(id = 1, name = "Alice", email = "alice@example.com", auth0Id = "auth0|alice123")
        val blackPlayer = User(id = 2, name = "Bob", email = "bob@example.com", auth0Id = "auth0|bob123")
        val expectedMatch = Match(
            id = 1,
            whitePlayer = whitePlayer,
            blackPlayer = blackPlayer,
            status = MatchStatus.PENDING,
            result = MatchResult.ONGOING
        )

        every { userRepository.findByIdOrNull(1) } returns whitePlayer
        every { userRepository.findByIdOrNull(2) } returns blackPlayer
        every { matchRepository.save(any()) } returns expectedMatch

        // When
        val result = matchService.createMatch(1, 2)

        // Then
        assertNotNull(result)
        assertEquals(MatchStatus.PENDING, result.status)
        assertEquals(MatchResult.ONGOING, result.result)
        verify { userRepository.findByIdOrNull(1) }
        verify { userRepository.findByIdOrNull(2) }
        verify { matchRepository.save(any()) }
    }

    @Test
    fun `createMatch should throw UserNotFoundException when white player does not exist`() {
        // Given
        every { userRepository.findByIdOrNull(1) } returns null

        // When/Then
        assertThrows<UserNotFoundException> {
            matchService.createMatch(1, 2)
        }
        verify { userRepository.findByIdOrNull(1) }
        verify(exactly = 0) { matchRepository.save(any()) }
    }

    @Test
    fun `createMatch should throw UserNotFoundException when black player does not exist`() {
        // Given
        val whitePlayer = User(id = 1, name = "Alice", email = "alice@example.com", auth0Id = "auth0|alice123")
        every { userRepository.findByIdOrNull(1) } returns whitePlayer
        every { userRepository.findByIdOrNull(2) } returns null

        // When/Then
        assertThrows<UserNotFoundException> {
            matchService.createMatch(1, 2)
        }
        verify { userRepository.findByIdOrNull(1) }
        verify { userRepository.findByIdOrNull(2) }
        verify(exactly = 0) { matchRepository.save(any()) }
    }

    @Test
    fun `updateMatch should update status and result`() {
        // Given
        val whitePlayer = User(id = 1, name = "Alice", email = "alice@example.com", auth0Id = "auth0|alice123")
        val blackPlayer = User(id = 2, name = "Bob", email = "bob@example.com", auth0Id = "auth0|bob123")
        val existingMatch = Match(
            id = 1,
            whitePlayer = whitePlayer,
            blackPlayer = blackPlayer,
            status = MatchStatus.IN_PROGRESS,
            result = MatchResult.ONGOING
        )
        val updatedMatch = Match(
            id = 1,
            whitePlayer = whitePlayer,
            blackPlayer = blackPlayer,
            status = MatchStatus.COMPLETED,
            result = MatchResult.WHITE_WIN
        )

        every { matchRepository.findByIdOrNull(1) } returns existingMatch
        every { matchRepository.save(any()) } returns updatedMatch

        // When
        val result = matchService.updateMatch(1, MatchStatus.COMPLETED, MatchResult.WHITE_WIN)

        // Then
        assertNotNull(result)
        assertEquals(MatchStatus.COMPLETED, result.status)
        assertEquals(MatchResult.WHITE_WIN, result.result)
        verify { matchRepository.findByIdOrNull(1) }
        verify { matchRepository.save(any()) }
    }

    @Test
    fun `updateMatch should throw MatchNotFoundException when match does not exist`() {
        // Given
        every { matchRepository.findByIdOrNull(1) } returns null

        // When/Then
        assertThrows<MatchNotFoundException> {
            matchService.updateMatch(1, MatchStatus.COMPLETED, MatchResult.WHITE_WIN)
        }
        verify { matchRepository.findByIdOrNull(1) }
        verify(exactly = 0) { matchRepository.save(any()) }
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

        every { userRepository.existsById(1) } returns true
        every { matchRepository.findByWhitePlayerIdOrBlackPlayerId(1, 1) } returns matches

        // When
        val result = matchService.getMatchesByPlayer(1)

        // Then
        assertNotNull(result)
        assertEquals(2, result.size)
        verify { userRepository.existsById(1) }
        verify { matchRepository.findByWhitePlayerIdOrBlackPlayerId(1, 1) }
    }

    @Test
    fun `getMatchesByPlayer should throw UserNotFoundException when player does not exist`() {
        // Given
        every { userRepository.existsById(1) } returns false

        // When/Then
        assertThrows<UserNotFoundException> {
            matchService.getMatchesByPlayer(1)
        }
        verify { userRepository.existsById(1) }
        verify(exactly = 0) { matchRepository.findByWhitePlayerIdOrBlackPlayerId(any(), any()) }
    }
}