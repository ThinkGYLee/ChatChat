package com.gyleedev.chatchat.data.repository

import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.gyleedev.chatchat.data.database.dao.MessageDao
import com.gyleedev.chatchat.data.database.entity.toEntity
import com.gyleedev.chatchat.data.database.entity.toUpdateEntity
import com.gyleedev.chatchat.domain.MessageData
import com.gyleedev.chatchat.domain.MessageSendState
import com.gyleedev.chatchat.domain.toRemoteModel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

interface MessageRepository {
    suspend fun insertMessageToLocal(message: MessageData, roomId: Long): Long?
    fun insertMessageToRemote(message: MessageData): Flow<MessageSendState>
    suspend fun updateMessageState(messageId: Long, roomId: Long, message: MessageData)
}

class MessageRepositoryImpl @Inject constructor(
    firebase: Firebase,
    private val messageDao: MessageDao
) : MessageRepository {

    val database =
        firebase.database("https://chat-a332d-default-rtdb.asia-southeast1.firebasedatabase.app/")

    override suspend fun insertMessageToLocal(message: MessageData, roomId: Long): Long? {
        return messageDao.insertMessage(
            message = message.toEntity(
                roomId = roomId
            )
        )
    }

    override fun insertMessageToRemote(message: MessageData): Flow<MessageSendState> =
        callbackFlow {
            database.reference.child("messages").child(message.chatRoomId)
                .child(message.time.toString())
                .setValue(message.toRemoteModel())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        trySend(MessageSendState.COMPLETE)
                    } else {
                        trySend(MessageSendState.FAIL)
                    }
                }
            awaitClose()
        }

    override suspend fun updateMessageState(
        messageId: Long,
        roomId: Long,
        message: MessageData
    ) {
        messageDao.updateMessageState(
            message = message.toUpdateEntity(
                messageId = messageId,
                roomId = roomId
            )
        )
    }
}
