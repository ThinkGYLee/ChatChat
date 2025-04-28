package com.gyleedev.chatchat.ui.friendlist

import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.filter
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.combine
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

    private val _noSuchUserAlert = MutableSharedFlow<Unit>()
    val noSuchUserAlert: SharedFlow<Unit> = _noSuchUserAlert

    private val notFriend = MutableStateFlow<List<Long>>(listOf())

    private val items = getFriendListScreenStateUseCase().cachedIn(viewModelScope)

    val updatedItem = combine(items, notFriend) { all, notFriends ->
        all.filter {
            it is FriendListUiState.Title ||
                it is FriendListUiState.MyData ||
                it is FriendListUiState.Loading ||
                (it is FriendListUiState.FriendData && it.friendData.id !in notFriends) ||
                (it is FriendListUiState.FavoriteData && it.favoriteData.id !in notFriends)
        }
    }

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
                updateNotFriend(relatedUserLocalData.id)
            } else {
                _noSuchUserAlert.emit(Unit)
            }
        }
    }

    fun hideFriend(relatedUserLocalData: RelatedUserLocalData?) {
        viewModelScope.launch {
            if (relatedUserLocalData != null) {
                hideFriendUseCase(relatedUserLocalData)
                updateNotFriend(relatedUserLocalData.id)
            } else {
                _noSuchUserAlert.emit(Unit)
            }
        }
    }

    fun blockFriend(relatedUserLocalData: RelatedUserLocalData?) {
        viewModelScope.launch {
            if (relatedUserLocalData != null) {
                blockFriendUseCase(relatedUserLocalData)
                updateNotFriend(relatedUserLocalData.id)
            } else {
                _noSuchUserAlert.emit(Unit)
            }
        }
    }

    private suspend fun updateNotFriend(id: Long) {
        val list = notFriend.value
        val updateList = mutableListOf(id)
        updateList.addAll(list)
        notFriend.emit(updateList)
    }

    private suspend fun uiRefresh() {
        _fetchJobDone.emit(Unit)
    }
}
