package com.gyleedev.chatchat.ui.myinfo

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.gyleedev.chatchat.domain.UserData
import com.gyleedev.chatchat.domain.usecase.GetMyDataFromRemoteUseCase
import com.gyleedev.core.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyInfoViewModel @Inject constructor(
    private val getMyDataFromRemoteUseCase: GetMyDataFromRemoteUseCase,
    savedStateHandle: SavedStateHandle
) : BaseViewModel() {
    private val _userData = MutableStateFlow(UserData())
    val userData: StateFlow<UserData?> = _userData

    init {
        viewModelScope.launch {
            val userUid = savedStateHandle.get<String>("myInfo")
            val myUserData = getMyDataFromRemoteUseCase().first()
            if (myUserData != null && userUid != null) {
                if (userUid == myUserData.uid) {
                    _userData.emit(myUserData)
                }
            }
        }
    }

    fun updateUser() {
        viewModelScope.launch {
            val myUserData = getMyDataFromRemoteUseCase().first()
            if (myUserData != null) {
                _userData.emit(myUserData)
            }
        }
    }
}
