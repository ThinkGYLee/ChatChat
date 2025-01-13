package com.gyleedev.chatchat.ui.myinfoedit

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
class MyInfoEditViewModel @Inject constructor(
    private val getMyUserDataUseCase: GetMyUserDataUseCase,
    savedStateHandle: SavedStateHandle
) : BaseViewModel() {
    private val _myUserData = MutableStateFlow(UserData())
    val myUserData: StateFlow<UserData?> = _myUserData

    private val _myNameQuery = MutableStateFlow("")
    val myNameQuery: StateFlow<String> = _myNameQuery

    private val _myStatusQuery = MutableStateFlow("")
    val myStatusQuery: StateFlow<String> = _myStatusQuery

    init {
        viewModelScope.launch {
            val userUid = savedStateHandle.get<String>("myInfo")
            val myUserData = getMyUserDataUseCase().first()
            if (myUserData != null && userUid != null) {
                if (userUid == myUserData.uid) {
                    _myUserData.emit(myUserData)
                    _myNameQuery.emit(myUserData.name)
                    _myStatusQuery.emit(myUserData.status)
                }
            }
        }
    }

    fun changeNameQuery(query: String) {
        viewModelScope.launch {
            _myNameQuery.emit(query)
        }
    }

    fun changeStatusQuery(query: String) {
        viewModelScope.launch {
            _myStatusQuery.emit(query)
        }
    }
}
