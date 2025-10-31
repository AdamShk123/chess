package com.example.chessandroid

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for Chess Android
 * @HiltAndroidApp triggers Hilt's code generation and sets up the application-level dependency container
 */
@HiltAndroidApp
class ChessApplication : Application()