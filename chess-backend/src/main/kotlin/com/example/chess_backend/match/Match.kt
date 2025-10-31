package com.example.chess_backend.match

import com.example.chess_backend.user.User
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.time.LocalDateTime

enum class MatchStatus {
    PENDING, IN_PROGRESS, COMPLETED, ABANDONED
}

enum class MatchResult {
    WHITE_WIN, BLACK_WIN, DRAW, ONGOING
}

@Entity
@Table(name = "matches")
class Match(
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,

    @field:ManyToOne(fetch = FetchType.LAZY)
    @field:JoinColumn(name = "white_player_id", nullable = false)
    val whitePlayer: User,

    @field:ManyToOne(fetch = FetchType.LAZY)
    @field:JoinColumn(name = "black_player_id", nullable = false)
    val blackPlayer: User,

    @field:Enumerated(EnumType.STRING)
    @field:Column(nullable = false, length = 20)
    val status: MatchStatus = MatchStatus.PENDING,

    @field:Enumerated(EnumType.STRING)
    @field:Column(nullable = false, length = 20)
    val result: MatchResult = MatchResult.ONGOING,

    @field:CreatedDate
    @field:Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @field:LastModifiedDate
    @field:Column(nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now()
)