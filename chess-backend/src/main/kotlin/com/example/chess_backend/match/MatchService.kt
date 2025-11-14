package com.example.chess_backend.match

import com.example.chess_backend.user.UserNotFoundException
import com.example.chess_backend.user.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class MatchService(
    private val matchRepository: MatchRepository,
    private val userRepository: UserRepository
) {
    fun getMatchesByPlayer(playerId: Int, pageable: Pageable): Page<Match> {
        if (!userRepository.existsById(playerId)) {
            throw UserNotFoundException(playerId)
        }
        return matchRepository.findByWhitePlayerIdOrBlackPlayerId(
            playerId,
            playerId,
            pageable
        )
    }
}