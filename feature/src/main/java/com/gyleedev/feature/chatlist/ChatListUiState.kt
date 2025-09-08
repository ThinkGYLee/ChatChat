package com.gyleedev.feature.chatlist

import androidx.paging.PagingData
import com.gyleedev.domain.model.ChatRoomDataWithAllRelatedUsersAndMessage
import kotlinx.coroutines.flow.Flow

sealed interface ChatListUiState {

    data object Loading : ChatListUiState

    data class Success(
        val pagingData: Flow<PagingData<ChatRoomDataWithAllRelatedUsersAndMessage>>
    ) : ChatListUiState
}

enum class ChatListLoadingState {
    SUCCESS,
    NONE,
    FAIL
}
