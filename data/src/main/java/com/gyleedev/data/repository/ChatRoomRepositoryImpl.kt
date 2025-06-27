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
import com.gyleedev.data.database.dao.ChatRoomAndReceiverDao
import com.gyleedev.data.database.dao.ChatRoomDao
import com.gyleedev.data.database.dao.ReceiverDao
import com.gyleedev.data.database.entity.ChatRoomEntity
import com.gyleedev.data.database.entity.ReceiverEntity
import com.gyleedev.data.database.entity.toEntity
import com.gyleedev.data.database.entity.toLocalData
import com.gyleedev.domain.model.ChatRoomAndReceiverLocalData
import com.gyleedev.domain.model.ChatRoomData
import com.gyleedev.domain.model.GetChatRoomException
import com.gyleedev.domain.model.GetChatRoomState
import com.gyleedev.domain.model.GetChatRoomState.CheckAndGetDataFromLocal
import com.gyleedev.domain.model.GetChatRoomState.CheckingFriendDataExists
import com.gyleedev.domain.model.GetChatRoomState.CheckingFriendReceiverExists
import com.gyleedev.domain.model.GetChatRoomState.CheckingMyDataExists
import com.gyleedev.domain.model.GetChatRoomState.CheckingMyReceiverExists
import com.gyleedev.domain.model.GetChatRoomState.CheckingRemoteGetChatRoomExists
import com.gyleedev.domain.model.GetChatRoomState.CompareAndInsertReceiversToLocal
import com.gyleedev.domain.model.GetChatRoomState.CreatingRemoteChatRoomData
import com.gyleedev.domain.model.GetChatRoomState.GetRemoteData
import com.gyleedev.domain.model.GetChatRoomState.GetRemoteReceivers
import com.gyleedev.domain.model.GetChatRoomState.InsertFriendDataToReceiver
import com.gyleedev.domain.model.GetChatRoomState.InsertMyDataToReceiver
import com.gyleedev.domain.model.GetChatRoomState.None
import com.gyleedev.domain.model.GetChatRoomState.ReturnChatRoom
import com.gyleedev.domain.model.GetChatRoomState.SavingGetChatRoomToLocal
import com.gyleedev.domain.model.GetChatRoomState.Success
import com.gyleedev.domain.model.GetChatRoomState.UpdateFriendData
import com.gyleedev.domain.model.GetChatRoomState.UpdateMyData
import com.gyleedev.domain.model.ProcessResult
import com.gyleedev.domain.model.RelatedUserLocalData
import com.gyleedev.domain.model.UserChatRoomData
import com.gyleedev.domain.model.UserChatRoomReceiver
import com.gyleedev.domain.repository.ChatRoomRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class ChatRoomRepositoryImpl @Inject constructor(
    private val chatRoomDao: ChatRoomDao,
    private val chatRoomAndReceiverDao: ChatRoomAndReceiverDao,
    private val receiverDao: ReceiverDao,
    private val database: FirebaseDatabase,
    private val auth: FirebaseAuth
) : ChatRoomRepository {

    private val _currentState: MutableStateFlow<GetChatRoomState> = MutableStateFlow(None)
    override val currentState: Flow<GetChatRoomState>
        get() = _currentState

    override suspend fun resetCurrentState() {
        _currentState.emit(None)
    }

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
    override suspend fun getChatRoomWithUserData(
        user: RelatedUserLocalData,
        getChatRoomState: GetChatRoomState
    ): GetChatRoomState {
        var chatRoomData: ChatRoomData? = null
        var localCurrentState = getChatRoomState
        _currentState.emit(getChatRoomState)
        try {
            while (true) {
                when (localCurrentState) {
                    CheckAndGetDataFromLocal -> {
                        val checkLocal = getPersonalChatRoomAndReceiverByUid(user.uid)
                        if (checkLocal != null) {
                            _currentState.emit(Success(checkLocal))
                            return Success(checkLocal)
                            break
                        } else {
                            _currentState.emit(CheckingRemoteGetChatRoomExists)
                            localCurrentState = CheckingRemoteGetChatRoomExists
                        }
                    }

                    CheckingRemoteGetChatRoomExists -> {
                        val checkRemote = checkChatRoomExistsInRemote(user).first()
                        localCurrentState = if (checkRemote) {
                            _currentState.emit(GetRemoteData)
                            GetRemoteData
                        } else {
                            _currentState.emit(CreatingRemoteChatRoomData)
                            CreatingRemoteChatRoomData
                        }
                    }

                    GetRemoteData -> {
                        chatRoomData = getChatRoomFromRemote(user).first()
                        requireChatRoomData(
                            data = chatRoomData,
                            problemState = GetRemoteData,
                            restartState = GetRemoteData
                        )
                        _currentState.emit(CheckingMyDataExists)
                        localCurrentState = CheckingMyDataExists
                    }

                    CheckingMyDataExists -> {
                        val isExists = checkMyRoomDataRemote(user).first()
                        localCurrentState = if (isExists) {
                            _currentState.emit(CheckingFriendDataExists)
                            CheckingFriendDataExists
                        } else {
                            _currentState.emit(UpdateMyData)
                            UpdateMyData
                        }
                    }

                    CheckingFriendDataExists -> {
                        val isExists = checkFriendRoomDataRemote(user).first()
                        localCurrentState = if (isExists) {
                            _currentState.emit(CheckingMyReceiverExists)
                            CheckingMyReceiverExists
                        } else {
                            _currentState.emit(UpdateFriendData)
                            UpdateFriendData
                        }
                    }

                    CreatingRemoteChatRoomData -> {
                        val createdRoomData = createChatRoomData().first()
                        if (createdRoomData != null) {
                            chatRoomData = createdRoomData
                            _currentState.emit(UpdateMyData)
                            localCurrentState = UpdateMyData
                        } else {
                            throw GetChatRoomException(
                                message = "Can't create ChatRoom",
                                cause = null,
                                problemState = CreatingRemoteChatRoomData,
                                restartState = CreatingRemoteChatRoomData
                            )
                            break
                        }
                    }

                    UpdateMyData -> {
                        val data = requireChatRoomData(
                            data = chatRoomData,
                            problemState = UpdateMyData,
                            restartState = CheckingRemoteGetChatRoomExists
                        )
                        val result = createMyUserChatRoom(user, data).first()
                        if (result == ProcessResult.Success) {
                            _currentState.emit(CheckingFriendDataExists)
                            localCurrentState = CheckingFriendDataExists
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
                        val data = requireChatRoomData(
                            data = chatRoomData,
                            problemState = UpdateFriendData,
                            restartState = CheckingRemoteGetChatRoomExists
                        )
                        val result = createFriendUserChatRoom(user, data).first()
                        if (result == ProcessResult.Success) {
                            _currentState.emit(CheckingMyReceiverExists)
                            localCurrentState = CheckingMyReceiverExists
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

                    CheckingMyReceiverExists -> {
                        val data = requireChatRoomData(
                            data = chatRoomData,
                            problemState = CheckingMyReceiverExists,
                            restartState = CheckingRemoteGetChatRoomExists
                        )
                        auth.currentUser?.uid?.let {
                            val result = checkReceiverRemote(data.rid, it).first()
                            if (result) {
                                _currentState.emit(CheckingFriendReceiverExists)
                                localCurrentState = CheckingFriendReceiverExists
                            } else {
                                _currentState.emit(InsertMyDataToReceiver)
                                localCurrentState = InsertMyDataToReceiver
                            }
                        }
                    }

                    CheckingFriendReceiverExists -> {
                        val data = requireChatRoomData(
                            data = chatRoomData,
                            problemState = CheckingFriendReceiverExists,
                            restartState = CheckingRemoteGetChatRoomExists
                        )
                        val result = checkReceiverRemote(data.rid, user.uid).first()
                        if (result) {
                            _currentState.emit(SavingGetChatRoomToLocal)
                            localCurrentState = SavingGetChatRoomToLocal
                        } else {
                            _currentState.emit(InsertFriendDataToReceiver)
                            localCurrentState = InsertFriendDataToReceiver
                        }
                    }

                    InsertMyDataToReceiver -> {
                        val data = requireChatRoomData(
                            data = chatRoomData,
                            problemState = InsertMyDataToReceiver,
                            restartState = CheckingRemoteGetChatRoomExists
                        )
                        auth.currentUser?.uid?.let {
                            val result = addReceiverToRemote(data.rid, uid = it).first()
                            if (result == ProcessResult.Success) {
                                _currentState.emit(CheckingFriendReceiverExists)
                                localCurrentState = CheckingFriendReceiverExists
                            } else {
                                throw GetChatRoomException(
                                    message = "Can't update FriendData",
                                    cause = null,
                                    problemState = InsertMyDataToReceiver,
                                    restartState = GetRemoteData
                                )
                            }
                        }
                    }

                    InsertFriendDataToReceiver -> {
                        val data = requireChatRoomData(
                            data = chatRoomData,
                            problemState = InsertFriendDataToReceiver,
                            restartState = CheckingRemoteGetChatRoomExists
                        )
                        val result = addReceiverToRemote(data.rid, uid = user.uid).first()
                        if (result == ProcessResult.Success) {
                            _currentState.emit(SavingGetChatRoomToLocal)
                            localCurrentState = SavingGetChatRoomToLocal
                        } else {
                            throw GetChatRoomException(
                                message = "Can't update FriendData",
                                cause = null,
                                problemState = InsertFriendDataToReceiver,
                                restartState = GetRemoteData
                            )
                        }
                    }

                    SavingGetChatRoomToLocal -> {
                        val data = requireChatRoomData(
                            data = chatRoomData,
                            problemState = SavingGetChatRoomToLocal,
                            restartState = CheckingRemoteGetChatRoomExists
                        )
                        val id = insertChatRoomToLocal(user, data, false)
                        insertReceiverToLocal(id, user.uid)
                        _currentState.emit(CheckAndGetDataFromLocal)
                        localCurrentState = CheckAndGetDataFromLocal
                    }

                    else -> {
                        throw IllegalStateException("Unknown state: $currentState")
                    }
                }
            }
        } catch (e: GetChatRoomException) {
            throw e
        }
    }

    private fun insertReceiverToLocal(userId: Long, uid: String) {
        receiverDao.insertReceiver(ReceiverEntity(userEntityId = userId, receiver = uid))
    }

    private fun getPersonalChatRoomAndReceiverByUid(
        uid: String,
        isGroup: Boolean = false
    ): ChatRoomAndReceiverLocalData? {
        return chatRoomAndReceiverDao.getChatRoomAndReceiverByUid(uid, isGroup)?.toLocalData()
    }

    override suspend fun createGroupChat(
        users: List<RelatedUserLocalData>,
        getChatRoomState: GetChatRoomState
    ): GetChatRoomState {
        TODO("Not yet implemented")
    }

    /*
    방번호로 체크하는 것
    이미 로컬에 존재하는 방번호로 가져올 때
    1. 로컬을 체크
        t -> 불러오기
        e -> exception 처음부터 시작
    2. 리모트 확인 receiver 가져오기
        t -> local receiver 와 비교하기
        f/3 -> 실패 처음부터 시작
    3. receiver 비교하기 (local 과 비교해서 차이가 있나 확인)
        t -> 같을 때 그대로 가져왔던 파일 success 로 내려보내기
        f -> 다를 때 없는 receiver 인서트
        e ->
    4. 없는 리시버 인서트
        t -> 다시 로컬 불러와서 return (1번으로 다시 가기)
        f/e -> 실패 2번부터 시작
     */
    override suspend fun getChatRoomWithRid(
        rid: String,
        getChatRoomState: GetChatRoomState
    ): GetChatRoomState {
        var chatRoomData: ChatRoomAndReceiverLocalData? = null
        var localCurrentState = getChatRoomState
        var insertReceivers: List<String>? = null
        _currentState.emit(getChatRoomState)
        try {
            while (true) {
                when (localCurrentState) {
                    CheckAndGetDataFromLocal -> {
                        val checkLocal = getChatRoomByRid(rid)
                        if (checkLocal != null) {
                            chatRoomData = checkLocal
                            _currentState.emit(GetRemoteReceivers)
                            localCurrentState = GetRemoteReceivers
                        } else {
                            throw GetChatRoomException(
                                message = "Room Problem",
                                cause = null,
                                problemState = CheckAndGetDataFromLocal,
                                restartState = CheckAndGetDataFromLocal
                            )
                        }
                    }

                    GetRemoteReceivers -> {
                        val data = requireChatRoomAndReceiver(
                            chatRoomData,
                            problemState = GetRemoteReceivers,
                            restartState = CheckAndGetDataFromLocal
                        )
                        val remoteReceivers = getReceiversFromRemote(rid).first().sortedDescending()
                        val localReceivers = data.receivers.sortedDescending()
                        if (remoteReceivers == localReceivers) {
                            _currentState.emit(ReturnChatRoom)
                            localCurrentState = ReturnChatRoom
                        } else {
                            val setRemote = remoteReceivers.toSet()
                            val setLocal = localReceivers.toSet()
                            val intersection = setRemote intersect setLocal
                            insertReceivers =
                                (setRemote + setLocal).filterNot { it in intersection }
                            _currentState.emit(CompareAndInsertReceiversToLocal)
                            localCurrentState = CompareAndInsertReceiversToLocal
                        }
                    }

                    CompareAndInsertReceiversToLocal -> {
                        val data = requireChatRoomAndReceiver(
                            chatRoomData,
                            problemState = CompareAndInsertReceiversToLocal,
                            restartState = CheckAndGetDataFromLocal
                        )
                        insertReceiversToLocal(data.id, requireNotNull(insertReceivers))
                        _currentState.emit(ReturnChatRoom)
                        localCurrentState = ReturnChatRoom
                    }

                    ReturnChatRoom -> {
                        val returnData = getChatRoomByRid(rid)
                        if (returnData != null) {
                            _currentState.emit(Success(returnData))
                            return Success(returnData)
                            break
                        } else {
                            throw GetChatRoomException(
                                message = "Room Problem",
                                cause = null,
                                problemState = ReturnChatRoom,
                                restartState = CheckAndGetDataFromLocal
                            )
                        }
                    }

                    else -> {
                        throw IllegalStateException("Unknown state: $currentState")
                    }
                }
            }
        } catch (e: GetChatRoomException) {
            throw e
        }
    }

    private fun insertReceiversToLocal(userEntityId: Long, list: List<String>) {
        val insertList = list.map {
            UserChatRoomReceiver(receiver = it).toEntity(userEntityId)
        }
        receiverDao.insertReceivers(insertList)
    }

    private fun getChatRoomData(
        users: List<RelatedUserLocalData>,
        getChatRoomState: GetChatRoomState
    ) {
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

    private fun requireChatRoomAndReceiver(
        data: ChatRoomAndReceiverLocalData?,
        problemState: GetChatRoomState,
        restartState: GetChatRoomState
    ): ChatRoomAndReceiverLocalData {
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
                        val result = snapshot.value != null
                        trySend(result)
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
                    problemState = CreatingRemoteChatRoomData,
                    restartState = CreatingRemoteChatRoomData,
                    message = requireNotNull(e.message),
                    cause = e.cause
                )
            }
            awaitClose()
        }.flowOn(Dispatchers.IO)

    private fun addReceiverToRemote(
        rid: String,
        uid: String
    ): Flow<ProcessResult> =
        callbackFlow {
            val isMe = auth.currentUser?.uid?.let { it == uid }
            try {
                database.reference.child("receivers").child(rid).child(uid)
                    .setValue((UserChatRoomReceiver(receiver = uid)))
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            trySend(ProcessResult.Success)
                        } else {
                            trySend(ProcessResult.Failure)
                        }
                    }
            } catch (e: Exception) {
                GetChatRoomException(
                    problemState = if (requireNotNull(isMe)) InsertMyDataToReceiver else InsertFriendDataToReceiver,
                    restartState = GetRemoteData,
                    message = requireNotNull(e.message),
                    cause = e.cause
                )
            }
            awaitClose()
        }.flowOn(Dispatchers.IO)

    private fun checkReceiverRemote(
        rid: String,
        uid: String
    ): Flow<Boolean> = callbackFlow {
        val isMe = auth.currentUser?.uid?.let { it == uid }
        try {
            database.reference.child("receivers").child(rid).orderByChild("receiver")
                .equalTo(uid).addListenerForSingleValueEvent(
                    object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val result = snapshot.value != null
                            trySend(result)
                        }

                        override fun onCancelled(error: DatabaseError) {
                            trySend(false)
                        }
                    }
                )
        } catch (e: Exception) {
            throw GetChatRoomException(
                cause = e.cause,
                message = e.message.toString(),
                problemState = if (requireNotNull(isMe)) CheckingMyReceiverExists else CheckingFriendReceiverExists,
                restartState = GetRemoteData
            )
        }

        awaitClose()
    }.flowOn(Dispatchers.IO)

    private fun getReceiversFromRemote(
        rid: String
    ): Flow<List<String>> = callbackFlow {
        try {
            database.reference.child("receivers").child(rid).get()
                .addOnSuccessListener { snapshot ->
                    println(snapshot.value)
                    val receivers = mutableListOf<String>()
                    if (snapshot.value != null) {
                        for (ds in snapshot.children) {
                            val snap = ds.getValue(UserChatRoomReceiver::class.java)
                            receivers.add(requireNotNull(snap?.receiver))
                        }
                    }
                    receivers.remove(auth.currentUser?.uid)
                    trySend(receivers)
                }.addOnFailureListener { task ->
                    throw Exception()
                }
        } catch (e: Exception) {
            throw GetChatRoomException(
                cause = e.cause,
                message = e.message.toString(),
                problemState = GetRemoteReceivers,
                restartState = CheckAndGetDataFromLocal
            )
        }
        awaitClose()
    }.flowOn(Dispatchers.IO)

    override fun createMyUserChatRoom(
        relatedUserLocalData: RelatedUserLocalData,
        chatRoomData: ChatRoomData
    ): Flow<ProcessResult> = callbackFlow {
        try {
            UserChatRoomData(rid = chatRoomData.rid)
            auth.currentUser?.uid?.let {
                database.reference.child("userChatRooms").child(it).child(chatRoomData.rid)
                    .setValue(
                        UserChatRoomData(rid = chatRoomData.rid)
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
                                val result = snapshot.value != null
                                trySend(result)
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
    }.flowOn(Dispatchers.IO)

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
                                val result = snapshot.value != null
                                trySend(result)
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
    }.flowOn(Dispatchers.IO)

    override fun createFriendUserChatRoom(
        relatedUserLocalData: RelatedUserLocalData,
        chatRoomData: ChatRoomData
    ): Flow<ProcessResult> = callbackFlow {
        try {
            auth.currentUser?.uid?.let {
                val userChatRoomData = UserChatRoomData(rid = chatRoomData.rid)
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

    override suspend fun makeNewChatRoom(rid: String, receiver: String, isGroup: Boolean): Long {
        return chatRoomDao.insertChatRoom(ChatRoomEntity(0, rid, "", isGroup))
    }

    /*override suspend fun getChatRoomByUid(uid: String): ChatRoomLocalData? {
        return try {
            chatRoomDao.getChatRoomByUid(uid)?.toModel()
        } catch (e: Exception) {
            throw GetChatRoomException(
                problemState = CheckAndGetDataFromLocal,
                restartState = CheckAndGetDataFromLocal,
                message = requireNotNull(e.message),
                cause = e.cause
            )
        }
    }*/

    private fun getChatRoomByRid(rid: String): ChatRoomAndReceiverLocalData? {
        return try {
            chatRoomAndReceiverDao.getChatRoomAndReceiverByRid(rid)?.toLocalData()
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
        chatRoomData: ChatRoomData,
        isGroup: Boolean
    ): Long {
        return chatRoomDao.insertChatRoom(
            ChatRoomEntity(
                id = 0L,
                lastMessage = "",
                rid = chatRoomData.rid,
                isGroup = isGroup
            )
        )
    }

    override suspend fun resetChatRoomData() {
        chatRoomDao.resetChatRoomDatabase()
    }

    override fun getChatRoomListWithPaging(): Flow<PagingData<ChatRoomAndReceiverLocalData>> {
        return Pager(
            config = PagingConfig(pageSize = 10, enablePlaceholders = false),
            pagingSourceFactory = {
                chatRoomAndReceiverDao.getChatRoomsWithPaging()
            }
        ).flow.map {
            it.map {
                it.toLocalData()
            }
        }.flowOn(Dispatchers.IO)
    }
}
