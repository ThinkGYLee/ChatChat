package com.gyleedev.chatchat.data.repository

import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.gyleedev.chatchat.data.database.dao.ChatRoomDao
import com.gyleedev.chatchat.data.database.dao.FriendDao
import com.gyleedev.chatchat.data.database.entity.ChatRoomEntity
import com.gyleedev.chatchat.data.database.entity.FriendEntity
import com.gyleedev.chatchat.data.database.entity.toEntity
import com.gyleedev.chatchat.data.database.entity.toFriendData
import com.gyleedev.chatchat.data.database.entity.toModel
import com.gyleedev.chatchat.domain.ChatRoomData
import com.gyleedev.chatchat.domain.ChatRoomDataWithFriend
import com.gyleedev.chatchat.domain.ChatRoomLocalData
import com.gyleedev.chatchat.domain.FriendData
import com.gyleedev.chatchat.domain.LogInResult
import com.gyleedev.chatchat.domain.SignInResult
import com.gyleedev.chatchat.domain.UserChatRoomData
import com.gyleedev.chatchat.domain.UserData
import com.gyleedev.chatchat.util.PreferenceUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

interface UserRepository {
    fun getUsersFromLocal(): List<UserData>
    suspend fun signInUser(id: String, password: String, nickname: String): Flow<UserData?>
    suspend fun loginRequest(id: String, password: String): Flow<LogInResult>
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
    fun checkChatRoomExistsInRemote(friendData: FriendData): Flow<Boolean>
    suspend fun createChatRoomData(): Flow<ChatRoomData?>
    suspend fun createMyUserChatRoom(
        friendData: FriendData,
        chatRoomData: ChatRoomData
    )

    suspend fun createFriendUserChatRoom(
        friendData: FriendData,
        chatRoomData: ChatRoomData
    )

    fun getFriendById(uid: String): Flow<FriendData>

    suspend fun makeNewChatRoom(rid: String, receiver: String): Long

    suspend fun getChatRoomByUid(uid: String): ChatRoomLocalData

    fun getMyUidFromLogInData(): String?

    fun getChatRoomIdFromRemote(friendData: FriendData): Flow<String?>
    fun getChatRoomFromRemote(friendData: FriendData): Flow<ChatRoomData?>
    suspend fun insertChatRoomToLocal(friendData: FriendData, chatRoomData: ChatRoomData): Long
    suspend fun getChatRoomListFromLocal(): Flow<PagingData<ChatRoomDataWithFriend>>
    suspend fun updateMyUserInfo(user: UserData): Flow<Boolean>
    suspend fun getFriendInfoFromRemote(uid: String): Flow<UserData?>
    fun getFriendListFromLocal(): Flow<List<FriendEntity>>
    suspend fun updateFriendInfoWithFriendEntity(friendEntity: FriendEntity)
    suspend fun updateFriendInfoByUid(uid: String)
    suspend fun logoutRequest()
}

class UserRepositoryImpl @Inject constructor(
    private val friendDao: FriendDao,
    firebase: Firebase,
    private val auth: FirebaseAuth,
    private val chatRoomDao: ChatRoomDao,
    private val preferenceUtil: PreferenceUtil
) : UserRepository {
    val database =
        firebase.database("https://chat-a332d-default-rtdb.asia-southeast1.firebasedatabase.app/")

    private val imageStorage = firebase.storage.getReference("image")

    override fun getUsersFromLocal(): List<UserData> {
        return friendDao.getUsers().map { it.toModel() }
    }

    override suspend fun signInUser(
        id: String,
        password: String,
        nickname: String
    ): Flow<UserData?> = callbackFlow {
        auth.createUserWithEmailAndPassword(id, password).addOnSuccessListener { task ->
            val myData = UserData(
                email = id,
                name = nickname,
                uid = task.user!!.uid,
                picture = " ",
                status = " "
            )
            preferenceUtil.setMyData(myData)
            trySend(myData)
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

    override suspend fun loginRequest(id: String, password: String): Flow<LogInResult> =
        callbackFlow {
            auth.signInWithEmailAndPassword(id, password).addOnSuccessListener {
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
                    for (ds in snapshot.children) {
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
        val myData = preferenceUtil.getMyData()
        if (myData.uid != "default uid" && auth.currentUser != null) {
            trySend(myData)
        } else {
            val query =
                database.reference.child("users").orderByChild("email")
                    .equalTo(auth.currentUser?.email)
            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (ds in snapshot.children) {
                        val snap = ds.getValue(UserData::class.java)
                        if (snap != null) {
                            preferenceUtil.setMyData(snap)
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
        }
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
                            for (ds in snapshot.children) {
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

    override suspend fun getFriendInfoFromRemote(uid: String): Flow<UserData?> = callbackFlow {
        val query = database.reference.child("users").orderByChild("uid").equalTo(uid)
        query.addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (ds in snapshot.children) {
                        val snap = ds.getValue(UserData::class.java)
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
            }
        )
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
                friendDao.getFriendsPaging()
            }
        ).flow.map { value ->
            value.map { it.toFriendData() }
        }
    }

    override suspend fun getFriendsCount(): Long {
        return friendDao.getFriendsCount()
    }

    override fun checkChatRoomExistsInRemote(friendData: FriendData): Flow<Boolean> =
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

    override fun getFriendById(uid: String): Flow<FriendData> {
        return friendDao.getFriendByUid(uid).map { it.toFriendData() }.flowOn(Dispatchers.IO)
    }

    override suspend fun getChatRoomByUid(uid: String): ChatRoomLocalData {
        return chatRoomDao.getChatRoomByUid(uid).toModel()
    }

    override fun getMyUidFromLogInData(): String? {
        return auth.currentUser?.uid
    }

    override fun getChatRoomIdFromRemote(friendData: FriendData): Flow<String?> =
        callbackFlow {
            auth.currentUser?.uid?.let {
                database.reference.child("userChatRooms").child(it).orderByChild("receiver")
                    .equalTo(friendData.uid)
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

    override fun getChatRoomFromRemote(friendData: FriendData): Flow<ChatRoomData?> =
        callbackFlow {
            auth.currentUser?.uid?.let {
                database.reference.child("userChatRooms").child(it).orderByChild("receiver")
                    .equalTo(friendData.uid)
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

    override suspend fun getChatRoomListFromLocal(): Flow<PagingData<ChatRoomDataWithFriend>> {
        return withContext(Dispatchers.IO) {
            val friends = getFriendsForChatRoomList()
            Pager(
                config = PagingConfig(pageSize = 10, enablePlaceholders = false),
                pagingSourceFactory = {
                    chatRoomDao.getChatRoomsWithPaging()
                }
            ).flow.map { value ->
                value.map { chatRoom ->
                    ChatRoomDataWithFriend(
                        chatRoomLocalData = chatRoom.toModel(),
                        friendData = friends.find { chatRoom.receiver == it.uid }!!
                    )
                }
            }
        }
    }

    private fun getFriendsForChatRoomList(): List<FriendData> {
        return friendDao.getFriends().map { it.toFriendData() }
    }

    override fun getFriendListFromLocal(): Flow<List<FriendEntity>> {
        return friendDao.getFriendsAsFlow().flowOn(Dispatchers.IO)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun updateMyUserInfo(user: UserData): Flow<Boolean> = callbackFlow {
        val imageUrl = uploadImageToRemote(user.picture.toUri()).first()
        val userData = UserData(
            email = user.email,
            name = user.name,
            status = user.status,
            uid = user.uid,
            picture = imageUrl
        )
        database.reference.child("users").child(user.uid).setValue(userData)
            .addOnSuccessListener {
                preferenceUtil.setMyData(userData)
                trySend(true)
            }.addOnFailureListener {
                trySend(false)
            }
        awaitClose()
    }

    override suspend fun updateFriendInfoWithFriendEntity(friendEntity: FriendEntity) {
        val remoteData = getFriendInfoFromRemote(friendEntity.uid).first()
        if (remoteData?.toEntity()?.toFriendData() != friendEntity.toFriendData()) {
            friendDao.updateUser(
                friendEntity.copy(
                    name = remoteData!!.name.ifBlank { friendEntity.name },
                    status = remoteData.status.ifBlank { friendEntity.status },
                    picture = remoteData.picture.ifBlank { friendEntity.picture }
                )
            )
        }
    }

    override suspend fun updateFriendInfoByUid(uid: String) {
        val localEntity = getFriendEntityFromLocalByUid(uid)
        val remoteData = getFriendInfoFromRemote(uid).first()
        if (localEntity.toFriendData() != remoteData?.toEntity()?.toFriendData()) {
            friendDao.updateUser(
                localEntity.copy(
                    name = remoteData!!.name.ifBlank { localEntity.name },
                    status = remoteData.status.ifBlank { localEntity.status },
                    picture = remoteData.picture.ifBlank { localEntity.picture }
                )
            )
        }
    }

    private suspend fun getFriendEntityFromLocalByUid(uid: String): FriendEntity {
        return friendDao.getFriendByUid(uid).first()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun uploadImageToRemote(uri: Uri): Flow<String> = callbackFlow {
        val fileName = Instant.now().toEpochMilli()
        val uuid = UUID.randomUUID().toString()
        val mountainsRef = imageStorage.child("$uuid$fileName.png")
        val uploadTask = mountainsRef.putFile(uri)
        uploadTask.addOnSuccessListener {
            trySend("$uuid$fileName.png")
        }.addOnFailureListener {
            trySend("")
        }
        awaitClose()
    }

    override suspend fun logoutRequest() {
        auth.signOut()
    }
}
