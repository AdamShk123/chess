package com.example.chessandroid.di

import android.content.Context
import com.example.chessandroid.R
import com.example.chessandroid.data.api.ChessApiService
import com.example.chessandroid.data.repository.IMatchHistoryRepository
import com.example.chessandroid.data.repository.IUserRepository
import com.example.chessandroid.data.repository.MatchHistoryRepository
import com.example.chessandroid.data.repository.UserRepository
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Hilt module that provides repository dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        @ApplicationContext context: Context,
        okHttpClient: OkHttpClient,
        json: Json
    ): Retrofit {
        val baseUrl = context.getString(R.string.api_base_url)
        val contentType = "application/json".toMediaType()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    fun provideChessApiService(retrofit: Retrofit): ChessApiService {
        return retrofit.create(ChessApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideMatchHistoryRepository(
        apiService: ChessApiService,
        userRepository: IUserRepository
    ): IMatchHistoryRepository {
        return MatchHistoryRepository(apiService, userRepository)
    }

    @Provides
    @Singleton
    fun provideUserRepository(
        @ApplicationContext context: Context
    ): IUserRepository {
        return UserRepository(context)
    }
}