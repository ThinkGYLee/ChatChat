package com.gyleedev.chatchat.ui.friendlist

import androidx.lifecycle.viewModelScope
import com.gyleedev.chatchat.core.BaseViewModel
import com.gyleedev.chatchat.data.model.RelatedUserRemoteData
import com.gyleedev.chatchat.domain.RelatedUserLocalData
import com.gyleedev.chatchat.domain.usecase.AddMyRelatedUsersUseCase
import com.gyleedev.chatchat.domain.usecase.BlockFriendUseCase
import com.gyleedev.chatchat.domain.usecase.DeleteFriendUseCase
import com.gyleedev.chatchat.domain.usecase.GetFriendListScreenStateUseCase
import com.gyleedev.chatchat.domain.usecase.GetFriendsCountUseCase
import com.gyleedev.chatchat.domain.usecase.GetMyRelatedUserListFromRemoteUseCase
import com.gyleedev.chatchat.domain.usecase.HideFriendUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FriendListViewModel @Inject constructor(
    private val getMyRelatedUserListFromRemoteUseCase: GetMyRelatedUserListFromRemoteUseCase,
    private val addMyRelatedUsersUseCase: AddMyRelatedUsersUseCase,
    private val getFriendsCountUseCase: GetFriendsCountUseCase,
    private val deleteFriendUseCase: DeleteFriendUseCase,
    private val hideFriendUseCase: HideFriendUseCase,
    getFriendListScreenStateUseCase: GetFriendListScreenStateUseCase,
    private val blockFriendUseCase: BlockFriendUseCase
) : BaseViewModel() {

    private val _fetchJobDone = MutableSharedFlow<Unit>()
    val fetchJobDone: SharedFlow<Unit> = _fetchJobDone

    val items = getFriendListScreenStateUseCase()

    private val _noSuchUserAlert = MutableSharedFlow<Unit>()
    val noSuchUserAlert: SharedFlow<Unit> = _noSuchUserAlert

    init {
        viewModelScope.launch {
            if (getFriendsCount() == 0L) {
                getMyRelatedUsersFromRemote()
            }
            uiRefresh()
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
            uiRefresh()
        }
    }

    fun hideFriend(relatedUserLocalData: RelatedUserLocalData?) {
        viewModelScope.launch {
            if (relatedUserLocalData != null) {
                hideFriendUseCase(relatedUserLocalData)
            } else {
                _noSuchUserAlert.emit(Unit)
            }
            uiRefresh()
        }
    }

    fun blockFriend(relatedUserLocalData: RelatedUserLocalData?) {
        viewModelScope.launch {
            if (relatedUserLocalData != null) {
                blockFriendUseCase(relatedUserLocalData)
            } else {
                _noSuchUserAlert.emit(Unit)
            }
            uiRefresh()
        }
    }

    private suspend fun uiRefresh() {
        _fetchJobDone.emit(Unit)
    }
}
