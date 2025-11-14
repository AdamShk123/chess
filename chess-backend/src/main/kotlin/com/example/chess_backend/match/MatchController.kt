package com.example.chess_backend.match

import com.example.chess_backend.auth.AuthenticationService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/matches")
class MatchController(
    private val matchService: MatchService,
    private val authenticationService: AuthenticationService
) {
    @GetMapping("/me")
    fun getMyMatches(
        @AuthenticationPrincipal jwt: Jwt?,
        @PageableDefault(page = 0, size = 10, sort = ["createdAt"], direction = Sort.Direction.DESC)
        pageable: Pageable
    ): ResponseEntity<Page<MatchResponse>> {
        val user = authenticationService.getCurrentUser(jwt)
        val matches = matchService.getMatchesByPlayer(user.id!!, pageable)
        return ResponseEntity.ok(matches.map { it.toResponse(user.id) })
    }
}