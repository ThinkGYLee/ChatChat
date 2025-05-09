package com.gyleedev.chatchat.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.gyleedev.chatchat.data.database.dao.MessageDao
import com.gyleedev.chatchat.data.database.entity.MessageEntity
import com.gyleedev.chatchat.data.database.entity.toEntity
import com.gyleedev.chatchat.data.database.entity.toModel
import com.gyleedev.chatchat.data.database.entity.toUpdateEntity
import com.gyleedev.chatchat.domain.ChatRoomLocalData
import com.gyleedev.chatchat.domain.MessageData
import com.gyleedev.chatchat.domain.MessageSendState
import com.gyleedev.chatchat.domain.ProcessResult
import com.gyleedev.chatchat.domain.UserRelationState
import com.gyleedev.chatchat.domain.toRemoteModel
import com.gyleedev.chatchat.util.PreferenceUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

interface MessageRepository {
    suspend fun insertMessageToLocal(message: MessageData, roomId: Long): Long
    fun insertMessageToRemote(message: MessageData): Flow<MessageSendState>
    suspend fun updateMessageState(messageId: Long, roomId: Long, message: MessageData)

    fun getMessageListener(
        chatRoom: ChatRoomLocalData,
        userRelationState: UserRelationState
    ): Flow<MessageData?>

    suspend fun getLastMessage(chatRoomId: String): MessageEntity?

    fun getMessagesFromLocal(rid: String): Flow<PagingData<MessageData>>

    fun getMessage(message: MessageData): Flow<MessageEntity>

    suspend fun deleteLocalMessage(messageId: Long)

    fun uploadImageToRemote(uri: String): Flow<String>

    suspend fun resetMessageData()

    suspend fun deleteRemoteMessage(message: MessageData): Flow<ProcessResult>

    suspend fun deleteMessageRequest(message: MessageData): Flow<ProcessResult>
}

class MessageRepositoryImpl @Inject constructor(
    firebase: Firebase,
    private val messageDao: MessageDao,
    private val preferenceUtil: PreferenceUtil
) : MessageRepository {

    val database =
        firebase.database("https://chat-a332d-default-rtdb.asia-southeast1.firebasedatabase.app/")

    private val imageStorage = firebase.storage.getReference("image")

    override suspend fun insertMessageToLocal(message: MessageData, roomId: Long): Long {
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
        }.flowOn(Dispatchers.IO)

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

    override fun getMessageListener(
        chatRoom: ChatRoomLocalData,
        userRelationState: UserRelationState
    ): Flow<MessageData?> {
        return messageListener(chatRoom)
            .onEach { messageData ->
                if (messageData != null && userRelationState != UserRelationState.BLOCKED) {
                    insertMessage(messageData, chatRoom.id)
                }
            }.flowOn(Dispatchers.IO)
    }

    private fun messageListener(chatRoom: ChatRoomLocalData): Flow<MessageData?> =
        callbackFlow {
            database.reference.child("messages").child(chatRoom.rid)
                .addChildEventListener(
                    object : ChildEventListener {
                        override fun onChildAdded(
                            snapshot: DataSnapshot,
                            previousChildName: String?
                        ) {
                            val snap = snapshot.getValue(MessageData::class.java)
                            if (snap != null) {
                                trySend(snap)
                                // insertMessage(snap, chatRoom.id)
                            }
                        }

                        override fun onChildChanged(
                            snapshot: DataSnapshot,
                            previousChildName: String?
                        ) {
                            trySend(null)
                            /*for (ds in snapshot.getChildren()) {
                                val snap = ds.getValue(MessageData::class.java)
                                if (snap != null) {
                                    insertMessage(snap, chatRoom.id)
                                }
                            }*/
                        }

                        override fun onChildRemoved(snapshot: DataSnapshot) {
                            trySend(null)
                        }

                        override fun onChildMoved(
                            snapshot: DataSnapshot,
                            previousChildName: String?
                        ) {
                            trySend(null)
                        }

                        override fun onCancelled(error: DatabaseError) {
                            trySend(null)
                        }
                    }
                )
            awaitClose()
        }

    private suspend fun insertMessage(message: MessageData, id: Long) {
        val lastMessage = getLastMessage(message.chatRoomId)
        if (lastMessage == null) {
            messageDao.insertMessage(message.toEntity(id))
        } else {
            if (message.time > lastMessage.time) {
                messageDao.insertMessage(message.toEntity(id))
            }
        }
    }

    override suspend fun getLastMessage(chatRoomId: String): MessageEntity? {
        return withContext(Dispatchers.IO) {
            try {
                messageDao.getLastMessage(chatRoomId)
            } catch (e: Exception) {
                println(e)
                null
            }
        }
    }

    override fun getMessagesFromLocal(rid: String): Flow<PagingData<MessageData>> {
        return Pager(
            config = PagingConfig(pageSize = 10, enablePlaceholders = false),
            pagingSourceFactory = {
                messageDao.getMessagesWithPaging(rid)
            }
        ).flow.map { value ->
            value.map { it.toModel() }
        }.flowOn(Dispatchers.IO)
    }

    override fun getMessage(message: MessageData): Flow<MessageEntity> {
        return messageDao.getMessage(
            rid = message.chatRoomId,
            writer = message.writer,
            time = message.time
        ).flowOn(Dispatchers.IO)
    }

    override suspend fun deleteLocalMessage(messageId: Long) {
        return messageDao.deleteMessage(messageId)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun uploadImageToRemote(uri: String): Flow<String> = callbackFlow {
        val fileName = Instant.now().toEpochMilli()
        val uuid = UUID.randomUUID().toString()
        val mountainsRef = imageStorage.child("$uuid$fileName.png")
        val uploadTask = mountainsRef.putFile(uri.toUri())
        uploadTask.addOnSuccessListener {
            trySend("$uuid$fileName.png")
        }.addOnFailureListener {
            trySend("")
        }
        awaitClose()
    }

    override suspend fun resetMessageData() {
        messageDao.resetMessageDatabase()
    }

    // 지우는 메시지의 작성자를 확인하고 본인이면 리모트에서도 삭제
    // 작성자가 다른사람이면 로컬에서만 삭제
    override suspend fun deleteMessageRequest(message: MessageData): Flow<ProcessResult> =
        callbackFlow {
            if (message.writer == preferenceUtil.getMyData().uid) {
                val remoteRequest = deleteRemoteMessage(message).first()
                if (remoteRequest == ProcessResult.Success) {
                    val messageEntity = getMessage(message).firstOrNull()
                    messageEntity?.let { deleteLocalMessage(it.id) }
                    trySend(ProcessResult.Success)
                } else {
                    trySend(ProcessResult.Failure)
                }
            } else {
                val messageEntity = getMessage(message).firstOrNull()
                messageEntity?.let { deleteLocalMessage(it.id) }
                trySend(ProcessResult.Success)
            }
            awaitClose()
        }

    override suspend fun deleteRemoteMessage(message: MessageData): Flow<ProcessResult> =
        callbackFlow {
            database.reference.child("messages").child(message.chatRoomId)
                .child(message.time.toString()).removeValue()
                .addOnSuccessListener {
                    trySend(ProcessResult.Success)
                }.addOnFailureListener {
                    trySend(ProcessResult.Failure)
                }
            awaitClose()
        }
}
