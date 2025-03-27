package com.gyleedev.chatchat.ui.myinfo

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.gyleedev.chatchat.core.BaseViewModel
import com.gyleedev.chatchat.domain.UserData
import com.gyleedev.chatchat.domain.usecase.GetMyUserDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyInfoViewModel @Inject constructor(
    private val getMyUserDataUseCase: GetMyUserDataUseCase,
    savedStateHandle: SavedStateHandle
) : BaseViewModel() {
    private val _userData = MutableStateFlow(UserData())
    val userData: StateFlow<UserData?> = _userData

    init {
        viewModelScope.launch {
            val userUid = savedStateHandle.get<String>("myInfo")
            val myUserData = getMyUserDataUseCase().first()
            if (myUserData != null && userUid != null) {
                if (userUid == myUserData.uid) {
                    _userData.emit(myUserData)
                }
            }
        }
    }

    fun updateUser() {
        viewModelScope.launch {
            val myUserData = getMyUserDataUseCase().first()
            if (myUserData != null) {
                _userData.emit(myUserData)
            }
        }
    }
}
