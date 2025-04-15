package com.gyleedev.chatchat.ui.friendlist

import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.gyleedev.chatchat.core.BaseViewModel
import com.gyleedev.chatchat.domain.FriendData
import com.gyleedev.chatchat.domain.UserData
import com.gyleedev.chatchat.domain.usecase.AddFriendsUseCase
import com.gyleedev.chatchat.domain.usecase.DeleteFriendUseCase
import com.gyleedev.chatchat.domain.usecase.GetFriendsCountUseCase
import com.gyleedev.chatchat.domain.usecase.GetFriendsUseCase
import com.gyleedev.chatchat.domain.usecase.GetMyFriendFromRemoteUseCase
import com.gyleedev.chatchat.domain.usecase.GetMyUserDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FriendListViewModel @Inject constructor(
    private val getMyUserDataUseCase: GetMyUserDataUseCase,
    getFriendsUseCase: GetFriendsUseCase,
    private val getMyFriendFromRemoteUseCase: GetMyFriendFromRemoteUseCase,
    private val addFriendsUseCase: AddFriendsUseCase,
    private val getFriendsCountUseCase: GetFriendsCountUseCase,
    private val deleteFriendUseCase: DeleteFriendUseCase
) : BaseViewModel() {

    private val _myUserData = MutableStateFlow<UserData?>(null)
    val myUserData: StateFlow<UserData?> = _myUserData

    val items = getFriendsUseCase().cachedIn(viewModelScope)

    private val _noSuchUserAlert = MutableSharedFlow<Unit>()
    val noSuchUserAlert: SharedFlow<Unit> = _noSuchUserAlert

    init {
        viewModelScope.launch {
            fetchMyUserData()
            if (getFriendsCount() == 0L) {
                getMyFriendsFromRemote()
            }
        }
    }

    fun fetchMyUserData() {
        viewModelScope.launch {
            val fetchUserdata = getMyUserDataUseCase().first()
            _myUserData.emit(fetchUserdata)
        }
    }

    private suspend fun getFriendsCount(): Long {
        return getFriendsCountUseCase()
    }

    private fun getMyFriendsFromRemote() {
        viewModelScope.launch {
            val request = getMyFriendFromRemoteUseCase().first()
            if (request != null) {
                addMyFriendsToLocal(request)
            }
        }
    }

    private suspend fun addMyFriendsToLocal(friends: List<UserData>) {
        addFriendsUseCase(friends)
    }

    fun deleteFriend(friendData: FriendData?) {
        viewModelScope.launch {
            if (friendData != null) {
                deleteFriendUseCase(friendData)
            } else {
                _noSuchUserAlert.emit(Unit)
            }
        }
    }
}
