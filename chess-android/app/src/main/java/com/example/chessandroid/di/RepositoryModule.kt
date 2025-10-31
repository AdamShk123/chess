package com.example.chessandroid.di

import android.content.Context
import com.example.chessandroid.data.repository.IMatchHistoryRepository
import com.example.chessandroid.data.repository.IUserRepository
import com.example.chessandroid.data.repository.MatchHistoryRepository
import com.example.chessandroid.data.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that provides repository dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideMatchHistoryRepository(): IMatchHistoryRepository {
        return MatchHistoryRepository()
    }

    @Provides
    @Singleton
    fun provideUserRepository(
        @ApplicationContext context: Context
    ): IUserRepository {
        return UserRepository(context)
    }
}