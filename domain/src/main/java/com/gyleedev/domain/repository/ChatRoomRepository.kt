package com.gyleedev.domain.repository

import androidx.paging.PagingData
import com.gyleedev.domain.model.ChatRoomAndReceiverLocalData
import com.gyleedev.domain.model.GetChatRoomState
import com.gyleedev.domain.model.RelatedUserLocalData
import kotlinx.coroutines.flow.Flow

interface ChatRoomRepository {
    val currentState: Flow<GetChatRoomState>
    suspend fun resetCurrentState()

    suspend fun getChatRoomWithUserData(
        user: RelatedUserLocalData,
        getChatRoomState: GetChatRoomState
    ): GetChatRoomState

    suspend fun getChatRoomWithRid(
        rid: String,
        getChatRoomState: GetChatRoomState
    ): GetChatRoomState

    suspend fun createGroupChat(
        users: List<RelatedUserLocalData>,
        getChatRoomState: GetChatRoomState = GetChatRoomState.CreateRemoteGroupChatRoom
    ): GetChatRoomState

    suspend fun resetChatRoomData()
    fun getChatRoomListWithPaging(): Flow<PagingData<ChatRoomAndReceiverLocalData>>
    suspend fun updateChatRooms(): Boolean
}
