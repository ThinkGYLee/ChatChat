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
import com.gyleedev.domain.model.GetChatRoomException
import com.gyleedev.domain.model.GetChatRoomState
import com.gyleedev.domain.model.GetChatRoomState.CheckAndGetDataFromLocal
import com.gyleedev.domain.model.GetChatRoomState.CheckingFriendDataExists
import com.gyleedev.domain.model.GetChatRoomState.CheckingMyDataExists
import com.gyleedev.domain.model.GetChatRoomState.CheckingRemoteGetChatRoomExists
import com.gyleedev.domain.model.GetChatRoomState.CreatingRemoteGetChatRoom
import com.gyleedev.domain.model.GetChatRoomState.GetRemoteData
import com.gyleedev.domain.model.GetChatRoomState.SavingGetChatRoomToLocal
import com.gyleedev.domain.model.GetChatRoomState.Success
import com.gyleedev.domain.model.GetChatRoomState.UpdateFriendData
import com.gyleedev.domain.model.GetChatRoomState.UpdateMyData
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
import kotlinx.coroutines.isActive
import java.util.UUID
import javax.inject.Inject

class ChatRoomRepositoryImpl @Inject constructor(
    private val chatRoomDao: ChatRoomDao,
    private val database: FirebaseDatabase,
    private val auth: FirebaseAuth
) : ChatRoomRepository {

    /*
    Flow 정리
    Local 확인
    a. Remote 에 Room 확인
    b. 내 챗룸에 Room 확인
    c. 친구 챗룸에 Room 확인
    a,b,c 가 모두 통과하면 가져와서 Local 에 Insert
    a,b,c 중에 한개라도 통과가 안되면 해당 지점 부터 Restart
    순서

    check local
    t -> return local data
    f -> check remote
    e -> problemState : CheckingLocal, restartState : CheckingLocal

    check remote roomdata exists
    t -> get data
        check remote mydata
    f -> create data
    e -> problemState : CheckingRoomExists, restartState: CheckingRoomExists

    get remoteData
    t -> check remote myData
    f/e -> problemState: GetRemoteData, restartState: GetRemoteData

    check remote mydata
    t -> check remote frienddata
    f -> update remote mydata
    e -> problemState: CheckingMyDataExists, restartState: CheckingRoomExists

    check remote frienddata
    t -> insert data to local
    f -> update remote frienddata
    e -> problemState: CheckingFirendDataExists, restartState: CheckingRoomExists

    create room
    t -> update mydata to remote
    f/e -> problemState: CreatingRoom, restartState: CreatingData

    update myData
    t -> update friendData to remote
    f/e -> problemState: updateMyData, restartState: CheckingRoomExists

    update friendData
    t -> insert local
    f/e -> problemState: updateFriendData, restartState: CheckingRoomExists

    insert data to local
    t -> get room data
    f/e -> problemState: InsertLocal, restartState; CheckingRoomExists

    get data
    t -> return
    f/e -> problemState: returnData, restartState: GetData

     */
    override fun getChatRoom(
        user: RelatedUserLocalData,
        getChatRoomState: GetChatRoomState
    ): Flow<GetChatRoomState> = callbackFlow {
        var currentState = getChatRoomState
        var chatRoomData: ChatRoomData? = null

        try {
            while (isActive) {
                when (currentState) {
                    CheckAndGetDataFromLocal -> {
                        send(CheckAndGetDataFromLocal)
                        val checkLocal = getChatRoomByUid(user.uid)
                        if (checkLocal != null) {
                            send(Success(checkLocal))
                            close()
                            break
                        }
                        currentState = CheckingRemoteGetChatRoomExists
                    }

                    CheckingRemoteGetChatRoomExists -> {
                        send(CheckingRemoteGetChatRoomExists)
                        val checkRemote = checkChatRoomExistsInRemote(user).first()
                        currentState = if (checkRemote) {
                            GetRemoteData
                        } else {
                            CreatingRemoteGetChatRoom
                        }
                    }

                    GetRemoteData -> {
                        send(GetRemoteData)
                        chatRoomData = getChatRoomFromRemote(user).first()
                        requireChatRoomData(
                            data = chatRoomData,
                            problemState = GetRemoteData,
                            restartState = GetRemoteData
                        )
                        currentState = CheckingMyDataExists
                    }

                    CheckingMyDataExists -> {
                        send(CheckingMyDataExists)
                        val isExists = checkMyRoomDataRemote(user).first()
                        currentState = if (isExists) {
                            CheckingFriendDataExists
                        } else {
                            UpdateMyData
                        }
                    }

                    CheckingFriendDataExists -> {
                        send(CheckingFriendDataExists)
                        val isExists = checkFriendRoomDataRemote(user).first()
                        currentState = if (isExists) {
                            SavingGetChatRoomToLocal
                        } else {
                            UpdateFriendData
                        }
                    }

                    CreatingRemoteGetChatRoom -> {
                        send(CreatingRemoteGetChatRoom)
                        val createdRoomData = createChatRoomData().first()
                        if (createdRoomData != null) {
                            currentState = UpdateMyData
                        } else {
                            throw GetChatRoomException(
                                message = "Can't create ChatRoom",
                                cause = null,
                                problemState = CreatingRemoteGetChatRoom,
                                restartState = CreatingRemoteGetChatRoom
                            )
                            break
                        }
                    }

                    UpdateMyData -> {
                        send(UpdateMyData)
                        val data = requireChatRoomData(
                            data = chatRoomData,
                            problemState = UpdateMyData,
                            restartState = CheckingRemoteGetChatRoomExists
                        )
                        val result = createMyUserChatRoom(user, data).first()
                        if (result == ProcessResult.Success) {
                            currentState = CheckingFriendDataExists
                        } else {
                            throw GetChatRoomException(
                                message = "Can't update MyData",
                                cause = null,
                                problemState = UpdateMyData,
                                restartState = GetRemoteData
                            )
                            break
                        }
                    }

                    UpdateFriendData -> {
                        send(UpdateFriendData)
                        val data = requireChatRoomData(
                            data = chatRoomData,
                            problemState = UpdateFriendData,
                            restartState = CheckingRemoteGetChatRoomExists
                        )
                        val result = createFriendUserChatRoom(user, data).first()
                        if (result == ProcessResult.Success) {
                            currentState = SavingGetChatRoomToLocal
                        } else {
                            throw GetChatRoomException(
                                message = "Can't update FriendData",
                                cause = null,
                                problemState = UpdateFriendData,
                                restartState = GetRemoteData
                            )
                            break
                        }
                    }

                    SavingGetChatRoomToLocal -> {
                        send(SavingGetChatRoomToLocal)
                        val data = requireChatRoomData(
                            data = chatRoomData,
                            problemState = SavingGetChatRoomToLocal,
                            restartState = CheckingRemoteGetChatRoomExists
                        )
                        insertChatRoomToLocal(user, data)
                        currentState = CheckAndGetDataFromLocal
                    }

                    else -> {
                        throw IllegalStateException("Unknown state: $currentState")
                    }
                }
            }
        } catch (e: GetChatRoomException) {
            println(e)
            close(e)
            throw e
        }
    }

    private fun requireChatRoomData(
        data: ChatRoomData?,
        problemState: GetChatRoomState,
        restartState: GetChatRoomState
    ): ChatRoomData {
        return data ?: throw GetChatRoomException(
            message = "No ChatRoomData",
            cause = null,
            problemState = problemState,
            restartState = restartState
        )
    }

    override fun checkChatRoomExistsInRemote(relatedUserLocalData: RelatedUserLocalData): Flow<Boolean> =
        callbackFlow {
            try {
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
            } catch (e: Exception) {
                GetChatRoomException(
                    problemState = CheckingRemoteGetChatRoomExists,
                    restartState = CheckingRemoteGetChatRoomExists,
                    message = requireNotNull(e.message),
                    cause = e.cause
                )
            }
            awaitClose()
        }.flowOn(Dispatchers.IO)

    override suspend fun createChatRoomData(): Flow<ChatRoomData?> =
        callbackFlow {
            try {
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
            } catch (e: Exception) {
                GetChatRoomException(
                    problemState = CreatingRemoteGetChatRoom,
                    restartState = CreatingRemoteGetChatRoom,
                    message = requireNotNull(e.message),
                    cause = e.cause
                )
            }
            awaitClose()
        }.flowOn(Dispatchers.IO)

    override fun createMyUserChatRoom(
        relatedUserLocalData: RelatedUserLocalData,
        chatRoomData: ChatRoomData
    ): Flow<ProcessResult> = callbackFlow {
        try {
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
        } catch (e: Exception) {
            GetChatRoomException(
                problemState = UpdateMyData,
                restartState = CheckingRemoteGetChatRoomExists,
                message = requireNotNull(e.message),
                cause = e.cause
            )
        }
        awaitClose()
    }.flowOn(Dispatchers.IO)

    private fun checkMyRoomDataRemote(
        relatedUserLocalData: RelatedUserLocalData
    ): Flow<Boolean> = callbackFlow {
        try {
            auth.currentUser?.uid?.let {
                database.reference.child("userChatRooms").child(it).orderByChild("receiver")
                    .equalTo(relatedUserLocalData.uid).addListenerForSingleValueEvent(
                        object : ValueEventListener {
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
                        }
                    )
            }
        } catch (e: Exception) {
            throw GetChatRoomException(
                cause = e.cause,
                message = e.message.toString(),
                problemState = CheckingMyDataExists,
                restartState = GetRemoteData
            )
        }

        awaitClose()
    }

    private fun checkFriendRoomDataRemote(
        relatedUserLocalData: RelatedUserLocalData
    ): Flow<Boolean> = callbackFlow {
        try {
            auth.currentUser?.uid?.let {
                database.reference.child("userChatRooms").child(relatedUserLocalData.uid)
                    .orderByChild("receiver")
                    .equalTo(it).addListenerForSingleValueEvent(
                        object : ValueEventListener {
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
                        }
                    )
            }
        } catch (e: Exception) {
            throw GetChatRoomException(
                cause = e.cause,
                message = e.message.toString(),
                problemState = CheckingFriendDataExists,
                restartState = GetRemoteData
            )
        }
        awaitClose()
    }

    override fun createFriendUserChatRoom(
        relatedUserLocalData: RelatedUserLocalData,
        chatRoomData: ChatRoomData
    ): Flow<ProcessResult> = callbackFlow {
        try {
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
        } catch (e: Exception) {
            GetChatRoomException(
                problemState = UpdateFriendData,
                message = requireNotNull(e.message),
                restartState = CheckingRemoteGetChatRoomExists,
                cause = e.cause
            )
        }
        awaitClose()
    }.flowOn(Dispatchers.IO)

    override suspend fun makeNewChatRoom(rid: String, receiver: String): Long {
        return chatRoomDao.insertChatRoom(ChatRoomEntity(0, rid, receiver, ""))
    }

    override suspend fun getChatRoomByUid(uid: String): ChatRoomLocalData {
        return try {
            chatRoomDao.getChatRoomByUid(uid).toModel()
        } catch (e: Exception) {
            throw GetChatRoomException(
                problemState = CheckAndGetDataFromLocal,
                restartState = CheckAndGetDataFromLocal,
                message = requireNotNull(e.message),
                cause = e.cause
            )
        }
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
        }.flowOn(Dispatchers.IO)

    override fun getChatRoomFromRemote(relatedUserLocalData: RelatedUserLocalData): Flow<ChatRoomData?> =
        callbackFlow {
            try {
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
            } catch (e: Exception) {
                GetChatRoomException(
                    problemState = CheckingRemoteGetChatRoomExists,
                    restartState = CheckingRemoteGetChatRoomExists,
                    message = requireNotNull(e.message),
                    cause = e.cause
                )
            }
            awaitClose()
        }.flowOn(Dispatchers.IO)

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
        }.flowOn(Dispatchers.IO)
    }
}
