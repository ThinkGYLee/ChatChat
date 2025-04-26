package com.gyleedev.chatchat.ui.friendinfo

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.gyleedev.chatchat.core.BaseViewModel
import com.gyleedev.chatchat.domain.RelatedUserLocalData
import com.gyleedev.chatchat.domain.usecase.BlockFriendUseCase
import com.gyleedev.chatchat.domain.usecase.DeleteFriendUseCase
import com.gyleedev.chatchat.domain.usecase.GetRelatedUserAndFavoriteDataUseCase
import com.gyleedev.chatchat.domain.usecase.HideFriendUseCase
import com.gyleedev.chatchat.domain.usecase.UpdateFavoriteByUserEntityIdUseCase
import com.gyleedev.chatchat.domain.usecase.UpdateFriendInfoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FriendInfoViewModel @Inject constructor(
    private val getRelatedUserAndFavoriteDataUseCase: GetRelatedUserAndFavoriteDataUseCase,
    private val updateFriendInfoUseCase: UpdateFriendInfoUseCase,
    private val deleteFriendUseCase: DeleteFriendUseCase,
    private val hideFriendUseCase: HideFriendUseCase,
    private val blockFriendUseCase: BlockFriendUseCase,
    private val updateFavoriteByUserEntityIdUseCase: UpdateFavoriteByUserEntityIdUseCase,
    savedStateHandle: SavedStateHandle
) : BaseViewModel() {
    private val _relatedUserLocalData = MutableStateFlow(RelatedUserLocalData())
    val relatedUserLocalData: StateFlow<RelatedUserLocalData?> = _relatedUserLocalData

    init {
        viewModelScope.launch {
            val userUid = savedStateHandle.get<String>("friend")
            if (userUid != null) {
                val friendData = getRelatedUserAndFavoriteDataUseCase(userUid).first()
                _relatedUserLocalData.emit(
                    friendData
                )
                updateFriendInfoUseCase(friendData.uid)
                _relatedUserLocalData.emit(
                    getRelatedUserAndFavoriteDataUseCase(friendData.uid).first()
                )
            }
        }
    }

    fun deleteFriend() {
        viewModelScope.launch {
            deleteFriendUseCase(_relatedUserLocalData.value)
        }
    }

    fun hideFriend() {
        viewModelScope.launch {
            hideFriendUseCase(_relatedUserLocalData.value)
        }
    }

    fun blockFriend() {
        viewModelScope.launch {
            blockFriendUseCase(_relatedUserLocalData.value)
        }
    }

    fun updateFavorite() {
        viewModelScope.launch {
            updateFavoriteByUserEntityIdUseCase(_relatedUserLocalData.value)
            updateUser()
        }
    }

    private suspend fun updateUser() {
        _relatedUserLocalData.emit(
            getRelatedUserAndFavoriteDataUseCase(_relatedUserLocalData.value.uid).first()
        )
    }
}
