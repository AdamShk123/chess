package com.example.chessandroid.data.api

import com.example.chessandroid.data.api.dto.MatchResponseDto
import com.example.chessandroid.data.api.dto.PageResponse
import com.example.chessandroid.data.api.dto.UserResponseDto
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

/**
 * Retrofit API service for chess backend endpoints
 */
interface ChessApiService {

    /**
     * Fetch paginated match history for the authenticated user
     * @param authorization Bearer token for authentication (format: "Bearer {token}")
     * @param pageNumber Page number (0-indexed)
     * @param pageSize Number of items per page
     * @param sort Sort field and direction (e.g., "createdAt,desc")
     */
    @GET("/api/matches/me")
    suspend fun getMyMatches(
        @Header("Authorization") authorization: String,
        @Query("page") pageNumber: Int = 0,
        @Query("size") pageSize: Int = 10,
        @Query("sort") sort: String = "createdAt,desc"
    ): PageResponse<MatchResponseDto>

    /**
     * Get the current authenticated user's profile
     * @param authorization Bearer token for authentication (format: "Bearer {token}")
     */
    @GET("/api/users/me")
    suspend fun getCurrentUser(
        @Header("Authorization") authorization: String
    ): UserResponseDto
}