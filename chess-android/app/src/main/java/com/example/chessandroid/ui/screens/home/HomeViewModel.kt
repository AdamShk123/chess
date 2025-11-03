package com.example.chessandroid.ui.screens.home

import androidx.lifecycle.ViewModel
import com.example.chessandroid.data.repository.IUserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userRepository: IUserRepository
) : ViewModel() {

    fun logout() {
        userRepository.logout()
    }
}
