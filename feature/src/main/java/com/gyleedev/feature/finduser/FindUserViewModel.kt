package com.gyleedev.feature.finduser

import androidx.lifecycle.viewModelScope
import com.gyleedev.core.BaseViewModel
import com.gyleedev.domain.model.ChangeRelationResult
import com.gyleedev.domain.model.SearchUserResult
import com.gyleedev.domain.model.UserData
import com.gyleedev.domain.usecase.AddFriendRequestUseCase
import com.gyleedev.domain.usecase.BlockUnknownUserUseCase
import com.gyleedev.domain.usecase.GetUserDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FindUserViewModel @Inject constructor(
    private val getUserDataUseCase: GetUserDataUseCase,
    private val addFriendRequestUseCase: AddFriendRequestUseCase,
    private val blockUnknownUserUseCase: BlockUnknownUserUseCase
) : BaseViewModel() {

    private val _emailQuery = MutableStateFlow("")

    private val _emailIsAvailable = MutableStateFlow(false)
    val emailIsAvailable: StateFlow<Boolean> = _emailIsAvailable

    private val _userData = MutableStateFlow<UserData?>(null)
    val userData: StateFlow<UserData?> = _userData

    private val _searchFailure = MutableSharedFlow<Unit>()
    val searchFailure: SharedFlow<Unit> = _searchFailure

    private val _userProcessComplete = MutableSharedFlow<FindProcessState>()
    val userProcessComplete: SharedFlow<FindProcessState> = _userProcessComplete

    fun editEmail(email: String) {
        viewModelScope.launch {
            _emailQuery.emit(email)
            emailValidator()
        }
    }

    private fun emailValidator() {
        viewModelScope.launch {
            val pattern = android.util.Patterns.EMAIL_ADDRESS
            _emailIsAvailable.emit(pattern.matcher(_emailQuery.value).matches())
        }
    }

    fun fetchUserData() {
        viewModelScope.launch {
            val fetchUserdata = getUserDataUseCase(_emailQuery.value).first()
            if (fetchUserdata is SearchUserResult.Failure) {
                _userProcessComplete.emit(FindProcessState.SearchFailure)
            } else {
                val userData = fetchUserdata as SearchUserResult.Success
                _userData.emit(userData.user)
            }
        }
    }

    fun addFriend() {
        viewModelScope.launch {
            val request = userData.value?.let { addFriendRequestUseCase(it).first() }
            if (request == true) {
                _userProcessComplete.emit(FindProcessState.Complete)
            } else {
                _userProcessComplete.emit(FindProcessState.AddFailure)
            }
        }
    }

    fun blockFriend() {
        viewModelScope.launch {
            val request = userData.value?.let { blockUnknownUserUseCase(it) }
            if (request == ChangeRelationResult.SUCCESS) {
                _userProcessComplete.emit(FindProcessState.Complete)
            } else {
                _userProcessComplete.emit(FindProcessState.BlockFailure)
            }
        }
    }
}
