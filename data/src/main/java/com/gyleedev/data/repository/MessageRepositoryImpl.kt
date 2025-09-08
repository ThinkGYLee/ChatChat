package com.gyleedev.data.repository

import android.net.Uri
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
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.gyleedev.data.database.dao.MessageDao
import com.gyleedev.data.database.entity.toEntity
import com.gyleedev.data.database.entity.toModel
import com.gyleedev.data.database.entity.toUpdateEntity
import com.gyleedev.data.preference.MyDataPreference
import com.gyleedev.domain.model.ChatRoomAndReceiverLocalData
import com.gyleedev.domain.model.MessageData
import com.gyleedev.domain.model.MessageSendState
import com.gyleedev.domain.model.MessageType
import com.gyleedev.domain.model.ProcessResult
import com.gyleedev.domain.model.UserRelationState
import com.gyleedev.domain.model.toRemoteModel
import com.gyleedev.domain.repository.MessageRepository
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

class MessageRepositoryImpl @Inject constructor(
    private val database: FirebaseDatabase,
    storage: FirebaseStorage,
    private val messageDao: MessageDao,
    private val myDataPreference: MyDataPreference
) : MessageRepository {

    private val imageStorageReference = storage.getReference("image")

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
        chatRoom: ChatRoomAndReceiverLocalData,
        userRelationState: UserRelationState
    ): Flow<MessageData?> {
        return messageListener(chatRoom)
            .onEach { messageData ->
                if (messageData != null && userRelationState != UserRelationState.BLOCKED) {
                    insertMessage(messageData, chatRoom.id)
                }
            }.flowOn(Dispatchers.IO)
    }

    private fun messageListener(chatRoom: ChatRoomAndReceiverLocalData): Flow<MessageData?> =
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

    override suspend fun getLastMessage(chatRoomId: String): MessageData {
        return withContext(Dispatchers.IO) {
            val message = messageDao.getLastMessage(chatRoomId)
            message?.toModel() ?: MessageData()
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

    override fun getMessage(message: MessageData): Flow<MessageData> {
        return messageDao.getMessage(
            rid = message.chatRoomId,
            writer = message.writer,
            time = message.time
        ).flowOn(Dispatchers.IO).map {
            it.toModel()
        }
    }

    override suspend fun deleteLocalMessage(messageId: Long) {
        return messageDao.deleteMessage(messageId)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun uploadAndGetImage(uri: Uri): Flow<String> = callbackFlow {
        val fileName = uploadImageToRemote(uri).first()
        val filePath = getImagePathByFirebase(fileName).first()
        trySend(filePath)
        awaitClose()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun uploadImageToRemote(uri: Uri): Flow<String> = callbackFlow {
        val fileName = Instant.now().toEpochMilli()
        val uuid = UUID.randomUUID().toString()
        val mountainsRef = imageStorageReference.child("$uuid$fileName.png")
        val uploadTask = mountainsRef.putFile(uri)
        uploadTask.addOnSuccessListener {
            trySend("$uuid$fileName.png")
        }.addOnFailureListener {
            trySend("")
        }
        awaitClose()
    }

    private fun getImagePathByFirebase(fileName: String): Flow<String> = callbackFlow {
        if (fileName != "") {
            val firebaseStorage = FirebaseStorage.getInstance().getReference("image")
            firebaseStorage.child(fileName).downloadUrl.addOnSuccessListener {
                trySend(it.toString())
            }.addOnFailureListener {
                trySend("")
            }
        } else {
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
            if (message.writer == myDataPreference.getMyData().uid) {
                val remoteRequest = deleteRemoteMessage(message).first()
                if (remoteRequest == ProcessResult.Success) {
                    val messageEntity = getMessage(message).firstOrNull()
                    messageEntity?.let { deleteLocalMessage(it.messageId) }
                    trySend(ProcessResult.Success)
                } else {
                    trySend(ProcessResult.Failure)
                }
            } else {
                val messageEntity = getMessage(message).firstOrNull()
                messageEntity?.let { deleteLocalMessage(it.messageId) }
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

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun sendMessage(messageData: MessageData, rid: Long, networkState: Boolean) {
        val message = if (messageData.type == MessageType.Photo) {
            messageData.copy(
                comment = uploadAndGetImage(messageData.comment.toUri()).first()
            )
        } else {
            messageData
        }
        val messageId = insertMessageToLocal(message, rid)
        if (networkState) {
            val request = try {
                insertMessageToRemote(message).first()
            } catch (e: Throwable) {
                println(e)
            }
            if (request is MessageSendState) {
                val updateMessage = message.copy(messageSendState = request)
                updateMessageState(messageId, rid, updateMessage)
            }
        } else {
            val updateMessage = message.copy(messageSendState = MessageSendState.FAIL)
            updateMessageState(messageId, rid, updateMessage)
        }
    }
}
