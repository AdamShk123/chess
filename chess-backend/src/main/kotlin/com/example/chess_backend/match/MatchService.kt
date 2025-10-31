package com.example.chess_backend.match

import com.example.chess_backend.user.UserNotFoundException
import com.example.chess_backend.user.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class MatchService(
    private val matchRepository: MatchRepository,
    private val userRepository: UserRepository
) {
    fun createMatch(whitePlayerId: Int, blackPlayerId: Int): Match {
        val whitePlayer = userRepository.findByIdOrNull(whitePlayerId)
            ?: throw UserNotFoundException(whitePlayerId)
        val blackPlayer = userRepository.findByIdOrNull(blackPlayerId)
            ?: throw UserNotFoundException(blackPlayerId)

        val match = Match(
            whitePlayer = whitePlayer,
            blackPlayer = blackPlayer,
            status = MatchStatus.PENDING,
            result = MatchResult.ONGOING
        )

        return matchRepository.save(match)
    }

    fun updateMatch(
        id: Int,
        status: MatchStatus?,
        result: MatchResult?
    ): Match {
        val match = matchRepository.findByIdOrNull(id)
            ?: throw MatchNotFoundException(id)

        val updated = Match(
            id = match.id,
            whitePlayer = match.whitePlayer,
            blackPlayer = match.blackPlayer,
            status = status ?: match.status,
            result = result ?: match.result,
            createdAt = match.createdAt
        )
        return matchRepository.save(updated)
    }

    fun getMatchesByPlayer(playerId: Int): List<Match> {
        if (!userRepository.existsById(playerId)) {
            throw UserNotFoundException(playerId)
        }
        return matchRepository.findByWhitePlayerIdOrBlackPlayerId(playerId, playerId)
    }
}