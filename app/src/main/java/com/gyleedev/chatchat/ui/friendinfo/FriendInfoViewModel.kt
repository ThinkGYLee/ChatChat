package com.gyleedev.chatchat.ui.friendinfo

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.gyleedev.chatchat.core.BaseViewModel
import com.gyleedev.chatchat.domain.RelatedUserLocalData
import com.gyleedev.chatchat.domain.usecase.BlockFriendUseCase
import com.gyleedev.chatchat.domain.usecase.DeleteFriendUseCase
import com.gyleedev.chatchat.domain.usecase.GetFriendDataUseCase
import com.gyleedev.chatchat.domain.usecase.HideFriendUseCase
import com.gyleedev.chatchat.domain.usecase.UpdateFriendInfoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FriendInfoViewModel @Inject constructor(
    private val getFriendDataUseCase: GetFriendDataUseCase,
    private val updateFriendInfoUseCase: UpdateFriendInfoUseCase,
    private val deleteFriendUseCase: DeleteFriendUseCase,
    private val hideFriendUseCase: HideFriendUseCase,
    private val blockFriendUseCase: BlockFriendUseCase,
    savedStateHandle: SavedStateHandle
) : BaseViewModel() {
    private val _relatedUserLocalData = MutableStateFlow(RelatedUserLocalData())
    val relatedUserLocalData: StateFlow<RelatedUserLocalData?> = _relatedUserLocalData

    init {
        viewModelScope.launch {
            val userUid = savedStateHandle.get<String>("friend")
            if (userUid != null) {
                val friendData = getFriendDataUseCase(userUid).first()
                _relatedUserLocalData.emit(
                    friendData
                )
                updateFriendInfoUseCase(friendData.uid)
                _relatedUserLocalData.emit(
                    getFriendDataUseCase(friendData.uid).first()
                )
            }
        }
    }

    fun deleteFriend() {
        viewModelScope.launch {
            deleteFriendUseCase(_relatedUserLocalData.value)
        }
    }
}
