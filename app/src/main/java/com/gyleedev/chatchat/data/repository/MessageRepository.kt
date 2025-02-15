package com.gyleedev.chatchat.data.repository

import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
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
import com.gyleedev.chatchat.domain.toRemoteModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import java.time.Instant
import javax.inject.Inject

interface MessageRepository {
    suspend fun insertMessageToLocal(message: MessageData, roomId: Long): Long
    fun insertMessageToRemote(message: MessageData): Flow<MessageSendState>
    suspend fun updateMessageState(messageId: Long, roomId: Long, message: MessageData)

    fun getMessageListener(chatRoom: ChatRoomLocalData): Flow<MessageData?>
    suspend fun getLastMessage(chatRoomId: String): MessageEntity?

    fun getMessagesFromLocal(rid: String): Flow<PagingData<MessageData>>

    fun getMessage(message: MessageData): Flow<MessageEntity>

    suspend fun deleteMessage(messageId: Long)

    suspend fun updateProfile()
}

class MessageRepositoryImpl @Inject constructor(
    firebase: Firebase,
    private val messageDao: MessageDao
) : MessageRepository {

    val database =
        firebase.database("https://chat-a332d-default-rtdb.asia-southeast1.firebasedatabase.app/")

    private val imageStorage = firebase.storage

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

    override fun getMessageListener(chatRoom: ChatRoomLocalData): Flow<MessageData?> {
        return messageListener(chatRoom)
            .onEach { messageData ->
                if (messageData != null) {
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

    override suspend fun deleteMessage(messageId: Long) {
        return messageDao.deleteMessage(messageId)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun uploadImage(uri: Uri): Flow<String> = callbackFlow {
        val storageRef = imageStorage.getReference("image")

        val fileName = Instant.now().toEpochMilli()
        val mountainsRef = storageRef.child("$fileName.png")

        val uploadTask = mountainsRef.putFile(uri)
        uploadTask.addOnSuccessListener {
            trySend("$fileName.png")
        }.addOnFailureListener {
            trySend("")
        }
        awaitClose()
    }

    override suspend fun updateProfile() {
        TODO("Not yet implemented")
    }
}
