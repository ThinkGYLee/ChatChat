package com.gyleedev.chatchat.ui.myinfoedit

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.gyleedev.chatchat.core.BaseViewModel
import com.gyleedev.chatchat.domain.UserData
import com.gyleedev.chatchat.domain.usecase.GetMyUserDataUseCase
import com.gyleedev.chatchat.domain.usecase.UpdateMyInfoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyInfoEditViewModel @Inject constructor(
    private val getMyUserDataUseCase: GetMyUserDataUseCase,
    private val updateMyInfoUseCase: UpdateMyInfoUseCase,
    savedStateHandle: SavedStateHandle
) : BaseViewModel() {
    private val _myUserData = MutableStateFlow(UserData())
    val myUserData: StateFlow<UserData?> = _myUserData

    private val _myNameQuery = MutableStateFlow("")
    val myNameQuery: StateFlow<String> = _myNameQuery

    private val _myStatusQuery = MutableStateFlow("")
    val myStatusQuery: StateFlow<String> = _myStatusQuery

    private val _myPictureAddress = MutableStateFlow("")
    val myPictureAddress: StateFlow<String> = _myPictureAddress

    private val _request = MutableSharedFlow<Boolean>()
    val request: SharedFlow<Boolean> = _request

    init {
        viewModelScope.launch {
            val userUid = savedStateHandle.get<String>("myInfo")
            val myUserData = getMyUserDataUseCase().first()
            if (myUserData != null && userUid != null) {
                if (userUid == myUserData.uid) {
                    _myUserData.emit(myUserData)
                    _myNameQuery.emit(myUserData.name)
                    _myStatusQuery.emit(myUserData.status)
                    _myPictureAddress.emit(myUserData.picture)
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

    fun changePictureUri(uri: Uri) {
        viewModelScope.launch {
            _myPictureAddress.emit(uri.toString())
        }
    }

    fun updateMyInfo() {
        viewModelScope.launch {
            val userData = _myUserData.value.copy(
                name = _myNameQuery.value,
                status = _myStatusQuery.value,
                picture = _myPictureAddress.value
            )
            _request.emit(updateMyInfoUseCase(userData).first())
        }
    }
}
