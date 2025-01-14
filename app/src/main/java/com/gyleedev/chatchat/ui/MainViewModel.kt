package com.gyleedev.chatchat.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gyleedev.chatchat.data.repository.UserRepository
import com.gyleedev.chatchat.domain.UserState
import com.gyleedev.chatchat.domain.usecase.UpdateFriendListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val updateFriendListUseCase: UpdateFriendListUseCase
) : ViewModel() {
    private val _isUserExists = MutableStateFlow(UserState.Loading)
    val isUserExists: StateFlow<UserState> = _isUserExists

    private val _startDestination = MutableStateFlow("")
    val startDestination: StateFlow<String> = _startDestination

    init {
        viewModelScope.launch {
            fetchUserExists()
            updateFriendListUseCase()
        }
    }

    private suspend fun fetchUserExists() {
        if (userRepository.fetchUserExists()) {
            _startDestination.emit(FRIENDLIST)
            _isUserExists.emit(UserState.Exists)
        } else {
            _isUserExists.emit(UserState.NoUser)
            _startDestination.emit(LOGIN)
        }
    }
}
