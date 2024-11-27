package com.gyleedev.chatchat.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.gyleedev.chatchat.data.database.dao.ChatRoomDao
import com.gyleedev.chatchat.data.database.dao.FriendDao
import com.gyleedev.chatchat.data.database.dao.MessageDao
import com.gyleedev.chatchat.data.database.entity.ChatRoomEntity
import com.gyleedev.chatchat.data.database.entity.MessageEntity
import com.gyleedev.chatchat.data.database.entity.toEntity
import com.gyleedev.chatchat.data.database.entity.toFriendData
import com.gyleedev.chatchat.data.database.entity.toModel
import com.gyleedev.chatchat.data.database.entity.toUpdateEntity
import com.gyleedev.chatchat.domain.ChatRoomData
import com.gyleedev.chatchat.domain.ChatRoomLocalData
import com.gyleedev.chatchat.domain.FriendData
import com.gyleedev.chatchat.domain.LogInResult
import com.gyleedev.chatchat.domain.MessageData
import com.gyleedev.chatchat.domain.MessageSendState
import com.gyleedev.chatchat.domain.SignInResult
import com.gyleedev.chatchat.domain.UserChatRoomData
import com.gyleedev.chatchat.domain.UserData
import com.gyleedev.chatchat.domain.toRemoteModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

interface UserRepository {
    fun getUsersFromLocal(): List<UserData>
    suspend fun signInUser(id: String, password: String): Flow<UserData?>
    suspend fun logInRequest(id: String, password: String): Flow<LogInResult>
    suspend fun searchUser(email: String): Flow<UserData?>
    fun fetchUserExists(): Boolean
    suspend fun writeUserToRemote(user: UserData): Flow<SignInResult>
    suspend fun getMyUserInformation(): Flow<UserData?>
    suspend fun addFriendToRemote(user: UserData): Flow<Boolean>
    suspend fun getMyFriendListFromRemote(): Flow<List<UserData>?>
    suspend fun insertMyFriendListToLocal(list: List<UserData>)
    suspend fun insertFriendToLocal(user: UserData)
    fun getFriends(): Flow<PagingData<FriendData>>
    suspend fun getFriendsCount(): Long
    suspend fun checkChatRoomExistsInRemote(friendData: FriendData): Flow<Boolean>
    suspend fun createChatRoomData(): Flow<ChatRoomData?>
    suspend fun createMyUserChatRoom(
        friendData: FriendData,
        chatRoomData: ChatRoomData
    )

    suspend fun createFriendUserChatRoom(
        friendData: FriendData,
        chatRoomData: ChatRoomData
    )

    suspend fun getFriendById(
        uid: String
    ): FriendData

    suspend fun makeNewChatRoom(rid: String, receiver: String): Long

    suspend fun getChatRoomByUid(uid: String): ChatRoomLocalData
    suspend fun insertMessageToLocal(message: MessageData, roomId: Long): Long?

    fun getMessagesFromLocal(rid: String): Flow<PagingData<MessageData>>
    fun getMyUidFromLogInData(): String?

    suspend fun insertMessageToRemote(message: MessageData): Flow<MessageSendState>
    suspend fun updateMessageState(messageId: Long, roomId: Long, message: MessageData)
    suspend fun getChatRoomIdFromRemote(friendData: FriendData): Flow<String?>
    suspend fun getChatRoomFromRemote(friendData: FriendData): Flow<ChatRoomData?>
    suspend fun insertChatRoomToLocal(friendData: FriendData, chatRoomData: ChatRoomData): Long
    suspend fun getMessageListener(chatRoom: ChatRoomLocalData)
    suspend fun getLastMessage(chatRoomId: String): MessageEntity?
}

class UserRepositoryImpl @Inject constructor(
    private val friendDao: FriendDao,
    firebase: Firebase,
    private val auth: FirebaseAuth,
    private val firebaseStorage: FirebaseStorage,
    private val chatRoomDao: ChatRoomDao,
    private val messageDao: MessageDao
) : UserRepository {
    val database =
        firebase.database("https://chat-a332d-default-rtdb.asia-southeast1.firebasedatabase.app/")

    override fun getUsersFromLocal(): List<UserData> {
        return friendDao.getUsers().map { it.toModel() }
    }

    override suspend fun signInUser(id: String, password: String): Flow<UserData?> = callbackFlow {
        auth.createUserWithEmailAndPassword(id, password).addOnSuccessListener { task ->
            trySend(
                UserData(
                    email = id,
                    name = "Anonymous User",
                    uid = task.user!!.uid,
                    picture = " ",
                    status = " "
                )
            )
        }.addOnFailureListener {
            trySend(null)
        }
        awaitClose()
    }

    override suspend fun writeUserToRemote(user: UserData): Flow<SignInResult> =
        callbackFlow {
            database.reference.child("users").child(user.uid).setValue(user)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        trySend(SignInResult.Success)
                    } else {
                        trySend(SignInResult.Failure)
                    }
                }
            awaitClose()
        }

    private fun writeUserToLocal(user: UserData) {
        friendDao.insertUser(user.toEntity())
    }

    override suspend fun logInRequest(id: String, password: String): Flow<LogInResult> =
        callbackFlow {
            auth.signInWithEmailAndPassword(id, password).addOnSuccessListener { task ->
                trySend(LogInResult.Success)
            }.addOnFailureListener { task ->
                val message = if (task.message != null) task.message else "unknown failure"
                trySend(LogInResult.Failure(message!!))
            }
            awaitClose()
        }

    override suspend fun searchUser(email: String): Flow<UserData?> = callbackFlow {
        val query =
            database.reference.child(
                "users"
            ).orderByChild("email").equalTo(email)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value != null) {
                    for (ds in snapshot.getChildren()) {
                        val snap = ds.getValue(UserData::class.java)
                        trySend(snap)
                    }
                } else {
                    trySend(null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                println(error)
                trySend(null)
            }
        })
        awaitClose()
    }

    override fun fetchUserExists(): Boolean {
        return auth.currentUser != null
    }

    override suspend fun getMyUserInformation(): Flow<UserData?> = callbackFlow {
        val query =
            database.reference.child("users").orderByChild("email").equalTo(auth.currentUser?.email)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (ds in snapshot.getChildren()) {
                    val snap = ds.getValue(UserData::class.java)
                    trySend(snap)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(null)
            }
        })
        awaitClose()
    }

    override suspend fun addFriendToRemote(user: UserData): Flow<Boolean> = callbackFlow {
        auth.currentUser?.let {
            database.reference.child("friends").child(it.uid).child(user.uid).setValue(user)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        trySend(true)
                    } else {
                        trySend(false)
                    }
                }
        }
        awaitClose()
    }

    override suspend fun getMyFriendListFromRemote(): Flow<List<UserData>?> = callbackFlow {
        auth.currentUser?.let {
            database.reference.child("friends").child(it.uid)
                .addListenerForSingleValueEvent(
                    object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val friendList = mutableListOf<UserData>()
                            for (ds in snapshot.getChildren()) {
                                val snap = ds.getValue(UserData::class.java)
                                if (snap != null) {
                                    friendList.add(snap)
                                }
                            }
                            trySend(friendList)
                        }

                        override fun onCancelled(error: DatabaseError) {
                            trySend(null)
                        }
                    }
                )
        }
        awaitClose()
    }

    override suspend fun insertMyFriendListToLocal(list: List<UserData>) {
        withContext(Dispatchers.IO) {
            friendDao.insertUsers(list.map { it.toEntity() })
        }
    }

    override suspend fun insertFriendToLocal(user: UserData) {
        friendDao.insertUser(user.toEntity())
    }

    override fun getFriends(): Flow<PagingData<FriendData>> {
        return Pager(
            config = PagingConfig(pageSize = 10, enablePlaceholders = false),
            pagingSourceFactory = {
                friendDao.getFriends()
            }
        ).flow.map { value ->
            value.map { it.toFriendData() }
        }
    }

    override suspend fun getFriendsCount(): Long {
        return friendDao.getFriendsCount()
    }

    override suspend fun checkChatRoomExistsInRemote(friendData: FriendData): Flow<Boolean> =
        callbackFlow {
            auth.currentUser?.uid?.let {
                database.reference.child("userChatRooms").child(it).orderByChild("receiver")
                    .equalTo(friendData.uid)
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
        }

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

    override suspend fun createMyUserChatRoom(
        friendData: FriendData,
        chatRoomData: ChatRoomData
    ) {
        UserChatRoomData(rid = chatRoomData.rid, receiver = friendData.uid)
        auth.currentUser?.uid?.let {
            database.reference.child("userChatRooms").child(it).child(chatRoomData.rid)
                .setValue(UserChatRoomData(rid = chatRoomData.rid, receiver = friendData.uid))
        }
    }

    override suspend fun createFriendUserChatRoom(
        friendData: FriendData,
        chatRoomData: ChatRoomData
    ) {
        auth.currentUser?.uid?.let {
            val userChatRoomData = UserChatRoomData(rid = chatRoomData.rid, receiver = it)
            database.reference.child("userChatRooms").child(friendData.uid)
                .child(chatRoomData.rid)
                .setValue(userChatRoomData)
        }
    }

    override suspend fun makeNewChatRoom(rid: String, receiver: String): Long {
        return chatRoomDao.insertChatRoom(ChatRoomEntity(0, rid, receiver, ""))
    }

    override suspend fun getFriendById(uid: String): FriendData {
        return friendDao.getFriendByUid(uid).toFriendData()
    }

    override suspend fun getChatRoomByUid(uid: String): ChatRoomLocalData {
        return chatRoomDao.getChatRoomByUid(uid).toModel()
    }

    override suspend fun insertMessageToLocal(message: MessageData, roomId: Long): Long? {
        println(message)
        return if (auth.currentUser?.uid != null) {
            messageDao.insertMessage(
                message = message.toEntity(
                    roomId = roomId
                )
            )
        } else {
            null
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
        }
    }

    override fun getMyUidFromLogInData(): String? {
        println(auth.currentUser)
        return auth.currentUser?.uid
    }

    override suspend fun insertMessageToRemote(message: MessageData): Flow<MessageSendState> =
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
        println(message)
        messageDao.updateMessageState(
            message = message.toUpdateEntity(
                messageId = messageId,
                roomId = roomId
            ).also {
                println(it)
            }
        )
    }

    override suspend fun getChatRoomIdFromRemote(friendData: FriendData): Flow<String?> =
        callbackFlow {
            auth.currentUser?.uid?.let {
                database.reference.child("userChatRooms").child(it).orderByChild("receiver")
                    .equalTo(friendData.uid)
            }?.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (ds in snapshot.getChildren()) {
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

    override suspend fun getChatRoomFromRemote(friendData: FriendData): Flow<ChatRoomData?> =
        callbackFlow {
            auth.currentUser?.uid?.let {
                database.reference.child("userChatRooms").child(it).orderByChild("receiver")
                    .equalTo(friendData.uid)
            }?.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (ds in snapshot.getChildren()) {
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
        friendData: FriendData,
        chatRoomData: ChatRoomData
    ): Long {
        return chatRoomDao.insertChatRoom(
            ChatRoomEntity(
                id = 0L,
                receiver = friendData.uid,
                lastMessage = "",
                rid = chatRoomData.rid
            )
        )
    }

    override suspend fun getMessageListener(chatRoom: ChatRoomLocalData) {
        database.reference.child("messages").child(chatRoom.rid)
            .addChildEventListener(
                object : ChildEventListener {

                    override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                        val snap = snapshot.getValue(MessageData::class.java)
                        if (snap != null) {
                            insertMessage(snap, chatRoom.id)
                        }
                    }

                    override fun onChildChanged(
                        snapshot: DataSnapshot,
                        previousChildName: String?
                    ) {
                        println(snapshot)
                        for (ds in snapshot.getChildren()) {
                            val snap = ds.getValue(MessageData::class.java)
                            if (snap != null) {
                                insertMessage(snap, chatRoom.id)
                            }
                        }
                    }

                    override fun onChildRemoved(snapshot: DataSnapshot) {
                        TODO("Not yet implemented")
                    }

                    override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                        TODO("Not yet implemented")
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                }
            )
    }

    private fun insertMessage(message: MessageData, id: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            auth.currentUser?.uid?.let {
                val lastMessage = getLastMessage(message.chatRoomId)
                if (lastMessage == null) {
                    messageDao.insertMessage(message.toEntity(id))
                } else {
                    if (message.writer != it) {
                        if (message.time > lastMessage.time) {
                            messageDao.insertMessage(message.toEntity(id))
                        }
                    }
                }
            }
        }
    }

    override suspend fun getLastMessage(chatRoomId: String): MessageEntity? {
        return try {
            messageDao.getLastMessage(chatRoomId)
        } catch (e: Exception) {
            null
        }
    }
}
