package com.gyleedev.chatchat.ui.friendlist

import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.gyleedev.core.BaseViewModel
import com.gyleedev.domain.model.RelatedUserLocalData
import com.gyleedev.domain.model.RelatedUserRemoteData
import com.gyleedev.domain.model.UserData
import com.gyleedev.domain.usecase.AddMyRelatedUsersUseCase
import com.gyleedev.domain.usecase.BlockRelatedUserUseCase
import com.gyleedev.domain.usecase.DeleteFriendUseCase
import com.gyleedev.domain.usecase.GetFavoritesUseCase
import com.gyleedev.domain.usecase.GetFriendsCountUseCase
import com.gyleedev.domain.usecase.GetFriendsUseCase
import com.gyleedev.domain.usecase.GetMyDataFromPreferenceUseCase
import com.gyleedev.domain.usecase.GetMyDataFromRemoteUseCase
import com.gyleedev.domain.usecase.GetMyRelatedUserListFromRemoteUseCase
import com.gyleedev.domain.usecase.HideFriendUseCase
import com.gyleedev.domain.usecase.UpdateFavoriteByUserEntityIdUseCase
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
    private val getMyRelatedUserListFromRemoteUseCase: GetMyRelatedUserListFromRemoteUseCase,
    private val addMyRelatedUsersUseCase: AddMyRelatedUsersUseCase,
    private val getFriendsCountUseCase: GetFriendsCountUseCase,
    private val deleteFriendUseCase: DeleteFriendUseCase,
    private val hideFriendUseCase: HideFriendUseCase,
    private val blockRelatedUserUseCase: BlockRelatedUserUseCase,
    getFriendsUseCase: GetFriendsUseCase,
    getFavoritesUseCase: GetFavoritesUseCase,
    private val getMyDataFromRemoteUseCase: GetMyDataFromRemoteUseCase,
    private val getMyDataFromPreferenceUseCase: GetMyDataFromPreferenceUseCase,
    private val updateFavoriteByUserEntityIdUseCase: UpdateFavoriteByUserEntityIdUseCase
) : BaseViewModel() {

    private val _noSuchUserAlert = MutableSharedFlow<Unit>()
    val noSuchUserAlert: SharedFlow<Unit> = _noSuchUserAlert

    // 친구 페이징 아이템
    val getFriends = getFriendsUseCase().cachedIn(viewModelScope)

    // 즐겨찾기 페이징 아이템
    val getFavorites = getFavoritesUseCase().cachedIn(viewModelScope)

    // 내 정보
    private val _myUserData = MutableStateFlow<UserData?>(UserData())
    val myUserData: StateFlow<UserData?> = _myUserData

    // 뷰모델 만들어질 때 친구 수가 0이라면 remote를 확인한 후 내 데이터를 가져온다.
    init {
        viewModelScope.launch {
            if (getFriendsCount() == 0L) {
                getMyRelatedUsersFromRemote()
            }
            getMyUserData()
        }
    }

    // preference에 내 정보 있나 확인하고 없으면 remote 에서 땡김
    private suspend fun getMyUserData() {
        _myUserData.emit(getMyDataFromRemoteUseCase().first())
    }

    // preference에서 내 정보 가져오기
    fun getMyUserFromPreference() {
        viewModelScope.launch {
            _myUserData.emit(getMyDataFromPreferenceUseCase())
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
                blockRelatedUserUseCase(relatedUserLocalData)
            } else {
                _noSuchUserAlert.emit(Unit)
            }
        }
    }

    fun updateFavorite(relatedUserLocalData: RelatedUserLocalData?) {
        viewModelScope.launch {
            if (relatedUserLocalData != null) {
                updateFavoriteByUserEntityIdUseCase(relatedUserLocalData)
            } else {
                _noSuchUserAlert.emit(Unit)
            }
        }
    }
}
