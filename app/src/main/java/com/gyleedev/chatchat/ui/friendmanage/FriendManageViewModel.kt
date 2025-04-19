package com.gyleedev.chatchat.ui.friendmanage

import androidx.lifecycle.viewModelScope
import com.gyleedev.chatchat.core.BaseViewModel
import com.gyleedev.chatchat.domain.UserData
import com.gyleedev.chatchat.domain.usecase.AddFriendRequestUseCase
import com.gyleedev.chatchat.domain.usecase.GetUserDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FriendManageViewModel @Inject constructor(
    private val getUserDataUseCase: GetUserDataUseCase,
    private val addFriendRequestUseCase: AddFriendRequestUseCase
) : BaseViewModel() {

    private val _emailQuery = MutableStateFlow("")

    private val _emailIsAvailable = MutableStateFlow(false)
    val emailIsAvailable: StateFlow<Boolean> = _emailIsAvailable

    private val _userData = MutableStateFlow<UserData?>(null)
    val userData: StateFlow<UserData?> = _userData

    private val _searchFailure = MutableSharedFlow<Unit>()
    val searchFailure: SharedFlow<Unit> = _searchFailure

    private val _addProcessComplete = MutableSharedFlow<Boolean>()
    val addProcessComplete: SharedFlow<Boolean> = _addProcessComplete

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
            val fetchUserdata = getUserDataUseCase(_emailQuery.value)
            fetchUserdata.collect { value ->
                if (value == null) {
                    _searchFailure.emit(Unit)
                } else {
                    _userData.emit(value)
                }
            }
        }
    }

    fun addFriend() {
        viewModelScope.launch {
            val request = userData.value?.let { addFriendRequestUseCase.invoke(it) }
            request?.collect { value ->
                _addProcessComplete.emit(value)
            }
        }
    }
}
