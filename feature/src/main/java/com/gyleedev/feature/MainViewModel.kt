package com.gyleedev.feature

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gyleedev.domain.model.UserState
import com.gyleedev.domain.usecase.FetchUserExistsUseCase
import com.gyleedev.domain.usecase.UpdateRelatedUserListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val fetchUserExistsUseCase: FetchUserExistsUseCase,
    private val updateRelatedUserListUseCase: UpdateRelatedUserListUseCase
) : ViewModel() {
    private val _isUserExists = MutableStateFlow(UserState.Loading)
    val isUserExists: StateFlow<UserState> = _isUserExists

    private val _startDestination = MutableStateFlow("")
    val startDestination: StateFlow<String> = _startDestination

    // TODO updateFriendListUseCase 가 없을대 어떻게 동작하는지 확인할 것
    init {
        viewModelScope.launch {
            fetchUserExists()
            updateRelatedUserListUseCase()
        }
    }

    private suspend fun fetchUserExists() {
        if (fetchUserExistsUseCase()) {
            _startDestination.emit(FRIENDLIST)
            _isUserExists.emit(UserState.Exists)
        } else {
            _isUserExists.emit(UserState.NoUser)
            _startDestination.emit(LOGIN)
        }
    }
}
