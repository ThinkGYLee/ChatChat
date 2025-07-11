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
import com.gyleedev.domain.model.GetChatRoomState.CreateRemoteGroupChatRoom
import com.gyleedev.domain.model.GetChatRoomState.CreatingRemoteChatRoomData
import com.gyleedev.domain.model.GetChatRoomState.GetAndSyncReceivers
import com.gyleedev.domain.model.GetChatRoomState.GetRemoteData
import com.gyleedev.domain.model.GetChatRoomState.InsertFriendDataToReceiver
import com.gyleedev.domain.model.GetChatRoomState.InsertMyDataToReceiver
import com.gyleedev.domain.model.GetChatRoomState.InsertReceiversToLocal
import com.gyleedev.domain.model.GetChatRoomState.InsertReceiversToRemote
import com.gyleedev.domain.model.GetChatRoomState.InsertUserChatRoomsToRemote
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
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
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
                        val result = createMyUserChatRoom(data).first()
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
                        val id = insertChatRoomToLocal(data, false)
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
        receiverDao.insertReceiver(ReceiverEntity(chatRoomEntityId = userId, receiver = uid))
    }

    private fun getPersonalChatRoomAndReceiverByUid(
        uid: String,
        isGroup: Boolean = false
    ): ChatRoomAndReceiverLocalData? {
        return chatRoomAndReceiverDao.getChatRoomAndReceiverByUid(uid, isGroup)?.toLocalData()
    }

    /*
    Flow 정리
    유저 리스트를 받아온다
    chatroom 정보로 가공하여 remote 에 전송
    chatroom 정보를 기반으로 각 유저의 유저챗룸에 데이터 전송
    chatroom 정보를 기반으로 receivers 에 데이터 전송
    위의 과정에 문제가 없으면 local에 insert
    위 과정에 문제가 있으면 remote 에 올라간 정보를 삭제한다.
    return local
     */
    override suspend fun createGroupChat(
        users: List<RelatedUserLocalData>,
        getChatRoomState: GetChatRoomState
    ): GetChatRoomState {
        var chatRoomData: ChatRoomData? = null
        var localCurrentState = getChatRoomState
        val userList = mutableListOf<String>()
        userList.addAll(users.map { it.uid })
        userList.add(requireNotNull(auth.currentUser?.uid))

        _currentState.emit(getChatRoomState)
        try {
            while (true) {
                when (localCurrentState) {
                    CreateRemoteGroupChatRoom -> {
                        chatRoomData = createChatRoomData().first()
                        if (chatRoomData != null) {
                            _currentState.emit(InsertUserChatRoomsToRemote)
                            localCurrentState = InsertUserChatRoomsToRemote
                        } else {
                            throw GetChatRoomException(
                                message = "Can't Make ChatRoom",
                                cause = null,
                                problemState = CreateRemoteGroupChatRoom,
                                restartState = CreateRemoteGroupChatRoom
                            )
                        }
                    }

                    InsertUserChatRoomsToRemote -> {
                        val result = insertUserChatRooms(
                            data = requireNotNull(chatRoomData),
                            uids = userList
                        )
                        if (result) {
                            _currentState.emit(InsertReceiversToRemote)
                            localCurrentState = InsertReceiversToRemote
                        } else {
                            throw GetChatRoomException(
                                message = "Can't Upload UserChatRooms",
                                cause = null,
                                problemState = InsertUserChatRoomsToRemote,
                                restartState = CreateRemoteGroupChatRoom
                            )
                        }
                    }

                    InsertReceiversToRemote -> {
                        val result = insertReceiversToRemote(
                            data = requireNotNull(chatRoomData),
                            uids = userList
                        )
                        if (result) {
                            _currentState.emit(SavingGetChatRoomToLocal)
                            localCurrentState = SavingGetChatRoomToLocal
                        } else {
                            throw GetChatRoomException(
                                message = "Can't Upload Receivers",
                                cause = null,
                                problemState = InsertReceiversToRemote,
                                restartState = CreateRemoteGroupChatRoom
                            )
                        }
                    }

                    SavingGetChatRoomToLocal -> {
                        val data = requireChatRoomData(
                            data = chatRoomData,
                            problemState = SavingGetChatRoomToLocal,
                            restartState = CheckingRemoteGetChatRoomExists
                        )
                        val id = insertChatRoomToLocal(data, true)
                        insertReceiversToLocal(id, users.map { it.uid })
                        _currentState.emit(ReturnChatRoom)
                        localCurrentState = ReturnChatRoom
                    }

                    ReturnChatRoom -> {
                        val returnData = getChatRoomByRid(requireNotNull(chatRoomData).rid)
                        if (returnData != null) {
                            _currentState.emit(Success(returnData))
                            return Success(returnData)
                            break
                        } else {
                            throw GetChatRoomException(
                                message = "Room Problem",
                                cause = null,
                                problemState = ReturnChatRoom,
                                restartState = CreateRemoteGroupChatRoom
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

    private suspend fun insertUserChatRooms(
        data: ChatRoomData,
        uids: List<String>
    ): Boolean = coroutineScope {
        val results = uids.map { uid ->
            async {
                runCatching {
                    createUserChatRoom(uid, data).first()
                }
            }
        }.awaitAll()

        return@coroutineScope results.all { it.isSuccess }
    }

    private suspend fun insertReceiversToRemote(
        data: ChatRoomData,
        uids: List<String>
    ): Boolean = coroutineScope {
        val results = uids.map { uid ->
            async {
                runCatching {
                    addReceiverToRemote(data.rid, uid).first()
                }
            }
        }.awaitAll()
        return@coroutineScope results.all { it.isSuccess }
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

    private fun getChatRoomDataFromRemote(rid: String): Flow<ChatRoomData?> = callbackFlow {
        database.reference.child("chatRooms").child(rid).get().addOnSuccessListener { snapshot ->
            println(snapshot.value)
            if (snapshot.value != null) {
                val snap = snapshot.getValue(ChatRoomData::class.java)
                trySend(snap)
            }
        }.addOnFailureListener {
            trySend(null)
        }
        awaitClose()
    }

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
                            _currentState.emit(GetAndSyncReceivers)
                            localCurrentState = GetAndSyncReceivers
                        } else {
                            throw GetChatRoomException(
                                message = "Room Problem",
                                cause = null,
                                problemState = CheckAndGetDataFromLocal,
                                restartState = CheckAndGetDataFromLocal
                            )
                        }
                    }

                    GetChatRoomState.StartFromGetRemoteAndInsertToLocal -> {
                        val localData = getChatRoomDataFromRemote(rid).first()
                        val remoteReceivers = getReceiversFromRemote(rid).first().sortedDescending()
                        val checkLocal = getChatRoomByRid(rid)
                        if (checkLocal == null) {
                            val id = insertChatRoomToLocal(
                                requireNotNull(localData),
                                isGroup = remoteReceivers.size > 1
                            )
                            chatRoomData =
                                ChatRoomAndReceiverLocalData(
                                    id = id,
                                    rid = rid,
                                    lastMessage = localData.lastMessage,
                                    isGroup = remoteReceivers.size > 1
                                )
                        } else {
                            chatRoomData = checkLocal
                        }
                        _currentState.emit(GetAndSyncReceivers)
                        localCurrentState = GetAndSyncReceivers
                    }

                    GetAndSyncReceivers -> {
                        val data = requireChatRoomAndReceiver(
                            chatRoomData,
                            problemState = GetAndSyncReceivers,
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
                            _currentState.emit(InsertReceiversToLocal)
                            localCurrentState = InsertReceiversToLocal
                        }
                    }

                    InsertReceiversToLocal -> {
                        val data = requireChatRoomAndReceiver(
                            chatRoomData,
                            problemState = InsertReceiversToLocal,
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

    private fun insertReceiversToLocal(chatRoomEntityId: Long, list: List<String>) {
        val insertList = list.map {
            UserChatRoomReceiver(receiver = it).toEntity(chatRoomEntityId)
        }
        receiverDao.insertReceivers(insertList)
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

    private fun checkChatRoomExistsInRemote(relatedUserLocalData: RelatedUserLocalData): Flow<Boolean> =
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

    private fun createChatRoomData(): Flow<ChatRoomData?> =
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
                problemState = GetAndSyncReceivers,
                restartState = CheckAndGetDataFromLocal
            )
        }
        awaitClose()
    }.flowOn(Dispatchers.IO)

    private fun createMyUserChatRoom(
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

    private fun createFriendUserChatRoom(
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

    private fun createUserChatRoom(
        uid: String,
        chatRoomData: ChatRoomData
    ): Flow<ProcessResult> = callbackFlow {
        try {
            auth.currentUser?.uid?.let {
                val userChatRoomData = UserChatRoomData(rid = chatRoomData.rid)
                database.reference.child("userChatRooms").child(uid)
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
                problemState = InsertUserChatRoomsToRemote,
                message = requireNotNull(e.message),
                restartState = CreateRemoteGroupChatRoom,
                cause = e.cause
            )
        }
        awaitClose()
    }.flowOn(Dispatchers.IO)

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

    fun getChatRoomFromRemote(relatedUserLocalData: RelatedUserLocalData): Flow<ChatRoomData?> =
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

    private fun insertChatRoomToLocal(
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

    private fun getUserChatRooms(): Flow<List<String>> = callbackFlow {
        auth.currentUser?.uid?.let {
            database.reference.child("userChatRooms").child(it).get()
                .addOnSuccessListener { snapshot ->
                    println(snapshot.value)
                    val chatRooms = mutableListOf<String>()
                    if (snapshot.value != null) {
                        for (ds in snapshot.children) {
                            val snap = ds.getValue(UserChatRoomData::class.java)
                            chatRooms.add(requireNotNull(snap?.rid))
                        }
                    }
                    trySend(chatRooms)
                }.addOnFailureListener {
                    trySend(emptyList())
                }
        }
        awaitClose()
    }

    private fun getReceiver(rid: String): Flow<List<String>> = callbackFlow {
        database.reference.child("receivers").child(rid).get()
            .addOnSuccessListener { snapshot ->
                val receivers = mutableListOf<String>()
                if (snapshot.value != null) {
                    for (ds in snapshot.children) {
                        val snap = ds.getValue(UserChatRoomReceiver::class.java)
                        receivers.add(requireNotNull(snap?.receiver))
                    }
                    trySend(receivers)
                }
            }.addOnFailureListener {
                trySend(emptyList())
            }
        awaitClose()
    }

    override suspend fun updateChatRooms(): Boolean = coroutineScope {
        val remoteChatRooms = getUserChatRooms().first().sorted()
        val localChatRooms = chatRoomDao.getChatRooms().map { it.rid }.sorted()
        var difference: List<String>? = null

        if (remoteChatRooms == localChatRooms) {
            return@coroutineScope true
        } else {
            val setRemote = remoteChatRooms.toSet()
            val setLocal = localChatRooms.toSet()

            val intersection = setRemote intersect setLocal
            difference =
                (setRemote + setLocal).filterNot { it in intersection }

            var count = 0
            difference.forEach {
                val chatroom =
                    getChatRoomWithRid(it, GetChatRoomState.StartFromGetRemoteAndInsertToLocal)
                if (chatroom is Success) {
                    count++
                }
            }
            return@coroutineScope count == difference.size
        }
    }
}
