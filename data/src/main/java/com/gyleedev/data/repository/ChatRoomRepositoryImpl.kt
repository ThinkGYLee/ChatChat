package com.gyleedev.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.gyleedev.data.database.dao.ChatRoomDao
import com.gyleedev.data.database.entity.ChatRoomEntity
import com.gyleedev.data.database.entity.toModel
import com.gyleedev.domain.model.ChatCreationState
import com.gyleedev.domain.model.ChatRoomData
import com.gyleedev.domain.model.ChatRoomLocalData
import com.gyleedev.domain.model.ProcessResult
import com.gyleedev.domain.model.RelatedUserLocalData
import com.gyleedev.domain.model.UserChatRoomData
import com.gyleedev.domain.repository.ChatRoomRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class ChatRoomRepositoryImpl @Inject constructor(
    private val chatRoomDao: ChatRoomDao,
    private val database: FirebaseDatabase,
    private val auth: FirebaseAuth
) : ChatRoomRepository {

    private fun getChatRoom(user: RelatedUserLocalData): Flow<ChatCreationState> = callbackFlow {
        send(ChatCreationState.CheckingLocal)
        try {
            val checkLocal = getChatRoomByUid(user.uid)
            if (checkLocal != null) {
                send(ChatCreationState.Success(checkLocal))
                close()
                return@callbackFlow
            }
            send(ChatCreationState.CheckingRemote)
            val checkRemote = checkChatRoomExistsInRemote(user).first()
            if (checkRemote) {
                send(ChatCreationState.SavingToLocal)
                val chatRoomData = getChatRoomFromRemote(user).first()
                insertChatRoomToLocal(user, requireNotNull(chatRoomData))
                val localData = getChatRoomByUid(user.uid)
                send(ChatCreationState.Success(localData))
                close()
                return@callbackFlow
            }
            send(ChatCreationState.CreatingRemoteChatRoom)
            val createdChatRoomData = createChatRoomData().first()
            if (createdChatRoomData != null) {
                send(ChatCreationState.UpdatingChatRoomToUserData)
                val myChatRoomInfo =
                    createMyUserChatRoom(user, requireNotNull(createdChatRoomData)).first()
                val friendChatRoomInfo =
                    createFriendUserChatRoom(user, requireNotNull(createdChatRoomData)).first()
                if (myChatRoomInfo == ProcessResult.Success && friendChatRoomInfo == ProcessResult.Success) {
                    send(ChatCreationState.SavingToLocal)
                    insertChatRoomToLocal(user, createdChatRoomData)
                    val localData = getChatRoomByUid(user.uid)
                    send(ChatCreationState.Success(localData))
                    close()
                    return@callbackFlow
                }
            }
        } catch (e: Exception) {
            send(
                ChatCreationState.Failure(
                    failurePoint = ChatCreationState.SavingToLocal
                )
            )
        }

    }

    override fun checkChatRoomExistsInRemote(relatedUserLocalData: RelatedUserLocalData): Flow<Boolean> =
        callbackFlow {
            auth.currentUser?.uid?.let {
                database.reference.child("userChatRooms").child(it).orderByChild("receiver")
                    .equalTo(relatedUserLocalData.uid)
            }?.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.value != null) {
                        trySend(true)
                    } else {
                        trySend(false)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    trySend(false)
                }
            })
            awaitClose()
        }.flowOn(Dispatchers.IO)

    override suspend fun createChatRoomData(): Flow<ChatRoomData?> =
        callbackFlow {
            val rid = UUID.randomUUID().toString()
            val chatRoomData = ChatRoomData(rid = rid, lastMessage = "")
            auth.currentUser?.uid?.let {
                database.reference.child("chatRooms").child(rid)
                    .setValue(ChatRoomData(rid = rid)).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            trySend(chatRoomData)
                        } else {
                            trySend(null)
                        }
                    }
            }
            awaitClose()
        }

    override fun createMyUserChatRoom(
        relatedUserLocalData: RelatedUserLocalData,
        chatRoomData: ChatRoomData
    ): Flow<ProcessResult> = callbackFlow {
        UserChatRoomData(rid = chatRoomData.rid, receiver = relatedUserLocalData.uid)
        auth.currentUser?.uid?.let {
            database.reference.child("userChatRooms").child(it).child(chatRoomData.rid)
                .setValue(
                    UserChatRoomData(
                        rid = chatRoomData.rid,
                        receiver = relatedUserLocalData.uid
                    )
                ).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        trySend(ProcessResult.Success)
                    } else {
                        trySend(ProcessResult.Failure)
                    }
                }
        }
    }

    override fun createFriendUserChatRoom(
        relatedUserLocalData: RelatedUserLocalData,
        chatRoomData: ChatRoomData
    ): Flow<ProcessResult> = callbackFlow {
        auth.currentUser?.uid?.let {
            val userChatRoomData = UserChatRoomData(rid = chatRoomData.rid, receiver = it)
            database.reference.child("userChatRooms").child(relatedUserLocalData.uid)
                .child(chatRoomData.rid)
                .setValue(userChatRoomData).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        trySend(ProcessResult.Success)
                    } else {
                        trySend(ProcessResult.Failure)
                    }
                }
        }
        awaitClose()
    }

    override suspend fun makeNewChatRoom(rid: String, receiver: String): Long {
        return chatRoomDao.insertChatRoom(ChatRoomEntity(0, rid, receiver, ""))
    }

    override suspend fun getChatRoomByUid(uid: String): ChatRoomLocalData {
        return chatRoomDao.getChatRoomByUid(uid).toModel()
    }

    override fun getChatRoomIdFromRemote(relatedUserLocalData: RelatedUserLocalData): Flow<String?> =
        callbackFlow {
            auth.currentUser?.uid?.let {
                database.reference.child("userChatRooms").child(it).orderByChild("receiver")
                    .equalTo(relatedUserLocalData.uid)
            }?.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (ds in snapshot.children) {
                        val snap = ds.getValue(ChatRoomData::class.java)
                        if (snap != null) {
                            trySend(snap.rid)
                        } else {
                            trySend(null)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    trySend(null)
                }
            })
            awaitClose()
        }

    override fun getChatRoomFromRemote(relatedUserLocalData: RelatedUserLocalData): Flow<ChatRoomData?> =
        callbackFlow {
            auth.currentUser?.uid?.let {
                database.reference.child("userChatRooms").child(it).orderByChild("receiver")
                    .equalTo(relatedUserLocalData.uid)
            }?.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (ds in snapshot.children) {
                        val snap = ds.getValue(ChatRoomData::class.java)
                        if (snap != null) {
                            trySend(snap)
                        } else {
                            trySend(null)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    trySend(null)
                }
            })
            awaitClose()
        }

    override suspend fun insertChatRoomToLocal(
        relatedUserLocalData: RelatedUserLocalData,
        chatRoomData: ChatRoomData
    ): Long {
        return chatRoomDao.insertChatRoom(
            ChatRoomEntity(
                id = 0L,
                receiver = relatedUserLocalData.uid,
                lastMessage = "",
                rid = chatRoomData.rid
            )
        )
    }

    override suspend fun resetChatRoomData() {
        chatRoomDao.resetChatRoomDatabase()
    }

    override fun getChatRoomListWithPaging(): Flow<PagingData<ChatRoomLocalData>> {
        return Pager(
            config = PagingConfig(pageSize = 10, enablePlaceholders = false),
            pagingSourceFactory = {
                chatRoomDao.getChatRoomsWithPaging()
            }
        ).flow.map {
            it.map {
                it.toModel()
            }
        }
    }
}
