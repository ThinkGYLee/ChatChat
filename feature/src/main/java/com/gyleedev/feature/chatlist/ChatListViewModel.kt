package com.gyleedev.feature.chatlist

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.gyleedev.core.BaseViewModel
import com.gyleedev.domain.repository.ChatRoomRepository
import com.gyleedev.domain.usecase.GetChatRoomDataWithRelatedUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatListViewModel @Inject constructor(
    getChatRoomDataWithRelatedUserUseCase: GetChatRoomDataWithRelatedUserUseCase,
    repository: ChatRoomRepository
) : BaseViewModel() {
    private val loadingState = MutableStateFlow(ChatListLoadingState.NONE)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState = loadingState
        .flatMapLatest { state ->
            if (state == ChatListLoadingState.SUCCESS) {
                getChatRoomDataWithRelatedUserUseCase()
            } else {
                flowOf(PagingData.empty())
            }
        }
        .cachedIn(viewModelScope)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            PagingData.empty()
        )

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.updateChatRooms()
            if (result) {
                loadingState.emit(ChatListLoadingState.SUCCESS)
            } else {
                loadingState.emit(ChatListLoadingState.FAIL)
            }
        }
    }
}
