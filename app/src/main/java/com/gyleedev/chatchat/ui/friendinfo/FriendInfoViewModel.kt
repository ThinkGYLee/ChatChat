package com.gyleedev.chatchat.ui.friendinfo

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.gyleedev.chatchat.core.BaseViewModel
import com.gyleedev.chatchat.domain.UserData
import com.gyleedev.chatchat.domain.usecase.GetFriendDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FriendInfoViewModel @Inject constructor(
    private val getFriendDataUseCase: GetFriendDataUseCase,
    savedStateHandle: SavedStateHandle
) : BaseViewModel() {
    private val _userData = MutableStateFlow(UserData())
    val userData: StateFlow<UserData?> = _userData

    init {
        viewModelScope.launch {
            val userUid = savedStateHandle.get<String>("friend")
            if (userUid != null) {
                val friendData = getFriendDataUseCase(userUid).first()
                _userData.emit(
                    UserData(
                        uid = friendData.uid,
                        picture = friendData.picture,
                        email = friendData.email,
                        status = friendData.status,
                        name = friendData.name
                    )
                )
            }
        }
    }
}
