package com.gyleedev.chatchat.ui.friendlist

import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.gyleedev.chatchat.core.BaseViewModel
import com.gyleedev.chatchat.data.model.RelatedUserRemoteData
import com.gyleedev.chatchat.domain.RelatedUserLocalData
import com.gyleedev.chatchat.domain.UserData
import com.gyleedev.chatchat.domain.usecase.AddMyRelatedUsersUseCase
import com.gyleedev.chatchat.domain.usecase.BlockFriendUseCase
import com.gyleedev.chatchat.domain.usecase.DeleteFriendUseCase
import com.gyleedev.chatchat.domain.usecase.GetFriendsCountUseCase
import com.gyleedev.chatchat.domain.usecase.GetFriendsUseCase
import com.gyleedev.chatchat.domain.usecase.GetMyRelatedUserListFromRemoteUseCase
import com.gyleedev.chatchat.domain.usecase.GetMyUserDataUseCase
import com.gyleedev.chatchat.domain.usecase.HideFriendUseCase
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
    private val getMyRelatedUserListFromRemoteUseCase: GetMyRelatedUserListFromRemoteUseCase,
    private val addMyRelatedUsersUseCase: AddMyRelatedUsersUseCase,
    private val getFriendsCountUseCase: GetFriendsCountUseCase,
    private val deleteFriendUseCase: DeleteFriendUseCase,
    private val hideFriendUseCase: HideFriendUseCase,
    private val blockFriendUseCase: BlockFriendUseCase
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
                getMyRelatedUsersFromRemote()
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

    private fun getMyRelatedUsersFromRemote() {
        viewModelScope.launch {
            val request = getMyRelatedUserListFromRemoteUseCase().first()
            if (request != null) {
                addMyRelatedUsersToLocal(request)
            }
        }
    }

    private suspend fun addMyRelatedUsersToLocal(friends: List<RelatedUserRemoteData>) {
        addMyRelatedUsersUseCase(friends)
    }

    fun deleteFriend(relatedUserLocalData: RelatedUserLocalData?) {
        viewModelScope.launch {
            if (relatedUserLocalData != null) {
                deleteFriendUseCase(relatedUserLocalData)
            } else {
                _noSuchUserAlert.emit(Unit)
            }
        }
    }

    fun hideFriend(relatedUserLocalData: RelatedUserLocalData?) {
        viewModelScope.launch {
            if (relatedUserLocalData != null) {
                hideFriendUseCase(relatedUserLocalData)
            } else {
                _noSuchUserAlert.emit(Unit)
            }
        }
    }

    fun blockFriend(relatedUserLocalData: RelatedUserLocalData?) {
        viewModelScope.launch {
            if (relatedUserLocalData != null) {
                blockFriendUseCase(relatedUserLocalData)
            } else {
                _noSuchUserAlert.emit(Unit)
            }
        }
    }
}
