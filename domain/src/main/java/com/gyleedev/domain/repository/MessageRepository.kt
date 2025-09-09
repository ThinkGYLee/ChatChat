package com.gyleedev.domain.repository

import androidx.paging.PagingData
import com.gyleedev.domain.model.ChatRoomAndReceiverLocalData
import com.gyleedev.domain.model.MessageData
import com.gyleedev.domain.model.MessageSendState
import com.gyleedev.domain.model.ProcessResult
import com.gyleedev.domain.model.UserRelationState
import kotlinx.coroutines.flow.Flow

interface MessageRepository {
    suspend fun insertMessageToLocal(message: MessageData, roomId: Long): Long
    fun insertMessageToRemote(message: MessageData): Flow<MessageSendState>
    suspend fun updateMessageState(messageId: Long, roomId: Long, message: MessageData)

    fun getMessageListener(
        chatRoom: ChatRoomAndReceiverLocalData,
        userRelationState: UserRelationState,
    ): Flow<MessageData?>

    suspend fun getLastMessage(chatRoomId: String): MessageData

    fun getMessagesFromLocal(rid: String): Flow<PagingData<MessageData>>

    fun getMessage(message: MessageData): Flow<MessageData>

    suspend fun deleteLocalMessage(messageId: Long)

    suspend fun resetMessageData()

    suspend fun deleteRemoteMessage(message: MessageData): Flow<ProcessResult>

    suspend fun deleteMessageRequest(message: MessageData): Flow<ProcessResult>
    suspend fun sendMessage(messageData: MessageData, rid: Long, networkState: Boolean)
}
