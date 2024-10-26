package com.gyleedev.chatchat.data.repository

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
import com.google.firebase.storage.FirebaseStorage
import com.gyleedev.chatchat.data.database.dao.ChatRoomDao
import com.gyleedev.chatchat.data.database.dao.FriendDao
import com.gyleedev.chatchat.data.database.dao.MessageDao
import com.gyleedev.chatchat.data.database.entity.ChatRoomEntity
import com.gyleedev.chatchat.data.database.entity.toEntity
import com.gyleedev.chatchat.data.database.entity.toFriendData
import com.gyleedev.chatchat.data.database.entity.toModel
import com.gyleedev.chatchat.domain.ChatRoomData
import com.gyleedev.chatchat.domain.ChatRoomLocalData
import com.gyleedev.chatchat.domain.FriendData
import com.gyleedev.chatchat.domain.LogInResult
import com.gyleedev.chatchat.domain.SignInResult
import com.gyleedev.chatchat.domain.UserChatRoomData
import com.gyleedev.chatchat.domain.UserData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
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
    suspend fun checkChatRoomExists(friendData: FriendData): Flow<Boolean>
    suspend fun createChatRoomData(): Flow<ChatRoomData?>
    suspend fun createMyUserChatRoom(
        friendData: FriendData,
        chatRoomData: ChatRoomData
    ): Flow<UserChatRoomData?>

    suspend fun createFriendUserChatRoom(
        friendData: FriendData,
        chatRoomData: ChatRoomData
    ): Flow<UserChatRoomData?>

    suspend fun getFriendById(
        uid: String
    ): FriendData

    suspend fun makeNewChatRoom(rid: String, receiver: String): Long

    suspend fun getChatRoomByUid(uid: String): ChatRoomLocalData
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

    override suspend fun checkChatRoomExists(friendData: FriendData): Flow<Boolean> = callbackFlow {
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
    ): Flow<UserChatRoomData?> = callbackFlow {
        val userChatRoomData = UserChatRoomData(rid = chatRoomData.rid, receiver = friendData.uid)
        auth.currentUser?.uid?.let {
            database.reference.child("userChatRooms").child(it).child(chatRoomData.rid)
                .setValue(UserChatRoomData(rid = chatRoomData.rid, receiver = friendData.uid))
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        trySend(userChatRoomData)
                    } else {
                        trySend(null)
                    }
                }
        }
        awaitClose()
    }

    override suspend fun createFriendUserChatRoom(
        friendData: FriendData,
        chatRoomData: ChatRoomData
    ): Flow<UserChatRoomData?> = callbackFlow {
        auth.currentUser?.uid?.let {
            val userChatRoomData = UserChatRoomData(rid = chatRoomData.rid, receiver = it)
            database.reference.child("userChatRooms").child(friendData.uid).child(chatRoomData.rid)
                .setValue(userChatRoomData).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        trySend(userChatRoomData)
                    } else {
                        trySend(null)
                    }
                }
        }
        awaitClose()
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
}
