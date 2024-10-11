package com.gyleedev.chatchat.ui.friendlist

import androidx.lifecycle.viewModelScope
import com.gyleedev.chatchat.core.BaseViewModel
import com.gyleedev.chatchat.domain.UserData
import com.gyleedev.chatchat.domain.usecase.GetMyUserDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FriendListViewModel @Inject constructor(
    private val useCase: GetMyUserDataUseCase
) : BaseViewModel() {
    private val _myUserData = MutableStateFlow<UserData?>(null)
    val myUserData: StateFlow<UserData?> = _myUserData

    init {
        fetchMyUserData()
    }

    private fun fetchMyUserData() {
        viewModelScope.launch {
            val fetchUserdata = useCase()
            fetchUserdata.collect { value ->
                _myUserData.emit(value)
                println(value)
            }
        }
    }
}
