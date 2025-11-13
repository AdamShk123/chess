package com.example.chess_backend.config

import com.example.chess_backend.match.Match
import com.example.chess_backend.match.MatchRepository
import com.example.chess_backend.match.MatchResult
import com.example.chess_backend.match.MatchStatus
import com.example.chess_backend.user.User
import com.example.chess_backend.user.UserRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class DataLoader(
    private val userRepository: UserRepository,
    private val matchRepository: MatchRepository
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        // Only load data if database is empty
        if (userRepository.count() > 0) {
            return
        }

        // Create users
        val alice = userRepository.save(User(
            name = "Alice",
            email = "alice@example.com",
            auth0Id = "auth0|alice-demo"
        ))

        val bob = userRepository.save(User(
            name = "Bob",
            email = "bob@example.com",
            auth0Id = "auth0|bob-demo"
        ))

        val charlie = userRepository.save(User(
            name = "Charlie",
            email = "charlie@example.com",
            auth0Id = "auth0|charlie-demo"
        ))

        userRepository.save(User(
            name = "Adam",
            email = "adam.shkolnik@outlook.com",
            auth0Id = "auth0|6909213e0bd9c60607317a9f"
        ))

        // Create matches
        matchRepository.save(Match(
            whitePlayer = alice,
            blackPlayer = bob,
            status = MatchStatus.COMPLETED,
            result = MatchResult.WHITE_WIN
        ))

        matchRepository.save(Match(
            whitePlayer = bob,
            blackPlayer = charlie,
            status = MatchStatus.IN_PROGRESS,
            result = MatchResult.ONGOING
        ))

        matchRepository.save(Match(
            whitePlayer = alice,
            blackPlayer = charlie,
            status = MatchStatus.PENDING,
            result = MatchResult.ONGOING
        ))

        println("Sample data loaded successfully!")
    }
}