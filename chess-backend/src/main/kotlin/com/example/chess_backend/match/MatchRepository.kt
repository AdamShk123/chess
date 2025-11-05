package com.example.chess_backend.match

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.ListCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface MatchRepository : ListCrudRepository<Match, Int> {
    fun findByWhitePlayerIdOrBlackPlayerId(whitePlayerId: Int, blackPlayerId: Int, pageable: Pageable): Page<Match>
}