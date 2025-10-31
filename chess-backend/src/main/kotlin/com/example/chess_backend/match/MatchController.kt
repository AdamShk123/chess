package com.example.chess_backend.match

import com.example.chess_backend.auth.AuthenticationService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*
import java.net.URI

@RestController
@RequestMapping("/api/matches")
class MatchController(
    private val matchService: MatchService,
    private val authenticationService: AuthenticationService
) {
    @GetMapping("/me")
    fun getMyMatches(@AuthenticationPrincipal jwt: Jwt?): ResponseEntity<List<MatchResponse>> {
        val user = authenticationService.getCurrentUser(jwt)
        val matches = matchService.getMatchesByPlayer(user.id!!)
        return ResponseEntity.ok(matches.map { it.toResponse() })
    }

    @PostMapping
    fun createMatch(@Valid @RequestBody request: CreateMatchRequest): ResponseEntity<MatchResponse> {
        val match = matchService.createMatch(request.whitePlayerId, request.blackPlayerId)
        return ResponseEntity
            .created(URI.create("/api/matches/${match.id}"))
            .body(match.toResponse())
    }

    @PutMapping("/{id}")
    fun updateMatch(
        @PathVariable id: Int,
        @Valid @RequestBody request: UpdateMatchRequest
    ): ResponseEntity<MatchResponse> {
        val match = matchService.updateMatch(id, request.status, request.result)
        return ResponseEntity.ok(match.toResponse())
    }
}