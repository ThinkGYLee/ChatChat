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
import com.gyleedev.chatchat.data.database.dao.UserDao
import com.gyleedev.chatchat.data.database.entity.ChatRoomEntity
import com.gyleedev.chatchat.data.database.entity.UserEntity
import com.gyleedev.chatchat.data.database.entity.toEntity
import com.gyleedev.chatchat.data.database.entity.toEntityAsFriend
import com.gyleedev.chatchat.data.database.entity.toModel
import com.gyleedev.chatchat.data.database.entity.toRelationLocalData
import com.gyleedev.chatchat.data.model.RelatedUserRemoteData
import com.gyleedev.chatchat.data.model.toRelatedUserLocalData
import com.gyleedev.chatchat.domain.ChangeRelationResult
import com.gyleedev.chatchat.domain.ChatRoomData
import com.gyleedev.chatchat.domain.ChatRoomDataWithRelatedUsers
import com.gyleedev.chatchat.domain.ChatRoomLocalData
import com.gyleedev.chatchat.domain.LogInResult
import com.gyleedev.chatchat.domain.RelatedUserLocalData
import com.gyleedev.chatchat.domain.SignInResult
import com.gyleedev.chatchat.domain.UserChatRoomData
import com.gyleedev.chatchat.domain.UserData
import com.gyleedev.chatchat.domain.UserRelationState
import com.gyleedev.chatchat.domain.toRemoteData
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
    suspend fun getMyRelatedUserListFromRemote(): Flow<List<RelatedUserRemoteData>?>
    suspend fun insertMyRelationsToLocal(list: List<RelatedUserRemoteData>)
    suspend fun insertFriendToLocal(user: UserData)
    fun getFriends(): Flow<PagingData<RelatedUserLocalData>>
    suspend fun getFriendsCount(): Long
    fun checkChatRoomExistsInRemote(relatedUserLocalData: RelatedUserLocalData): Flow<Boolean>
    suspend fun createChatRoomData(): Flow<ChatRoomData?>
    suspend fun createMyUserChatRoom(
        relatedUserLocalData: RelatedUserLocalData,
        chatRoomData: ChatRoomData
    )

    suspend fun createFriendUserChatRoom(
        relatedUserLocalData: RelatedUserLocalData,
        chatRoomData: ChatRoomData
    )

    fun getFriendById(uid: String): Flow<RelatedUserLocalData>

    suspend fun makeNewChatRoom(rid: String, receiver: String): Long

    suspend fun getChatRoomByUid(uid: String): ChatRoomLocalData

    fun getMyUidFromLogInData(): String?

    fun getChatRoomIdFromRemote(relatedUserLocalData: RelatedUserLocalData): Flow<String?>
    fun getChatRoomFromRemote(relatedUserLocalData: RelatedUserLocalData): Flow<ChatRoomData?>
    suspend fun insertChatRoomToLocal(
        relatedUserLocalData: RelatedUserLocalData,
        chatRoomData: ChatRoomData
    ): Long

    suspend fun getChatRoomListFromLocal(): Flow<PagingData<ChatRoomDataWithRelatedUsers>>
    suspend fun updateMyUserInfo(user: UserData): Flow<Boolean>
    suspend fun getUserInfoFromRemote(uid: String): Flow<UserData?>
    fun getRelatedUserListFromLocal(): Flow<List<UserEntity>>
    suspend fun updateRelatedUserInfoWithUserEntity(userEntity: UserEntity)
    suspend fun updateUserInfoByUid(uid: String)
    suspend fun logoutRequest()
    suspend fun resetFriendData()
    suspend fun resetMyUserData()
    suspend fun resetChatRoomData()
    fun setMyUserInformation(userData: UserData)
    suspend fun deleteFriendRequest(relatedUserLocalData: RelatedUserLocalData): ChangeRelationResult
    suspend fun changeRelationRemote(
        relatedUserLocalData: RelatedUserLocalData,
        relation: UserRelationState
    ): Flow<Boolean>

    suspend fun changeRelationLocal(
        relatedUserLocalData: RelatedUserLocalData,
        relation: UserRelationState
    ): Flow<ChangeRelationResult>

    suspend fun hideFriendRequest(relatedUserLocalData: RelatedUserLocalData): ChangeRelationResult
    suspend fun blockFriendRequest(relatedUserLocalData: RelatedUserLocalData): ChangeRelationResult
}

class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    firebase: Firebase,
    private val auth: FirebaseAuth,
    private val chatRoomDao: ChatRoomDao,
    private val preferenceUtil: PreferenceUtil
) : UserRepository {
    val database =
        firebase.database("https://chat-a332d-default-rtdb.asia-southeast1.firebasedatabase.app/")

    private val imageStorage = firebase.storage.getReference("image")

    override fun getUsersFromLocal(): List<UserData> {
        return userDao.getUsers().map { it.toModel() }
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
            setMyUserInformation(myData)
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
        userDao.insertUser(user.toEntityAsFriend())
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
                            setMyUserInformation(snap)
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
        val relation = RelatedUserRemoteData(
            uid = user.uid,
            email = user.email,
            name = user.name,
            picture = user.picture,
            status = user.status,
            userRelation = UserRelationState.FRIEND
        )
        auth.currentUser?.let {
            database.reference.child("relations").child(it.uid).child(user.uid).setValue(relation)
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

    override suspend fun getMyRelatedUserListFromRemote(): Flow<List<RelatedUserRemoteData>?> =
        callbackFlow {
            auth.currentUser?.let {
                database.reference.child("relations").child(it.uid)
                    .addListenerForSingleValueEvent(
                        object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val relationList = mutableListOf<RelatedUserRemoteData>()
                                for (ds in snapshot.children) {
                                    val snap = ds.getValue(RelatedUserRemoteData::class.java)
                                    if (snap != null) {
                                        relationList.add(snap)
                                    }
                                }
                                trySend(relationList)
                            }

                            override fun onCancelled(error: DatabaseError) {
                                trySend(null)
                            }
                        }
                    )
            }
            awaitClose()
        }

    override suspend fun getUserInfoFromRemote(uid: String): Flow<UserData?> = callbackFlow {
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

    override suspend fun insertMyRelationsToLocal(list: List<RelatedUserRemoteData>) {
        withContext(Dispatchers.IO) {
            userDao.insertUsers(list.map { it.toRelatedUserLocalData().toEntity() })
        }
    }

    override suspend fun insertFriendToLocal(user: UserData) {
        userDao.insertUser(user.toEntityAsFriend())
    }

    override fun getFriends(): Flow<PagingData<RelatedUserLocalData>> {
        return Pager(
            config = PagingConfig(pageSize = 10, enablePlaceholders = false),
            pagingSourceFactory = {
                userDao.getFriendsPaging()
            }
        ).flow.map { value ->
            value.map { it.toRelationLocalData() }
        }
    }

    override suspend fun getFriendsCount(): Long {
        return userDao.getFriendsCount()
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

    override suspend fun createMyUserChatRoom(
        relatedUserLocalData: RelatedUserLocalData,
        chatRoomData: ChatRoomData
    ) {
        UserChatRoomData(rid = chatRoomData.rid, receiver = relatedUserLocalData.uid)
        auth.currentUser?.uid?.let {
            database.reference.child("userChatRooms").child(it).child(chatRoomData.rid)
                .setValue(
                    UserChatRoomData(
                        rid = chatRoomData.rid,
                        receiver = relatedUserLocalData.uid
                    )
                )
        }
    }

    override suspend fun createFriendUserChatRoom(
        relatedUserLocalData: RelatedUserLocalData,
        chatRoomData: ChatRoomData
    ) {
        auth.currentUser?.uid?.let {
            val userChatRoomData = UserChatRoomData(rid = chatRoomData.rid, receiver = it)
            database.reference.child("userChatRooms").child(relatedUserLocalData.uid)
                .child(chatRoomData.rid)
                .setValue(userChatRoomData)
        }
    }

    override suspend fun makeNewChatRoom(rid: String, receiver: String): Long {
        return chatRoomDao.insertChatRoom(ChatRoomEntity(0, rid, receiver, ""))
    }

    override fun getFriendById(uid: String): Flow<RelatedUserLocalData> {
        return userDao.getUserInfoByUid(uid).map { it.toRelationLocalData() }.flowOn(Dispatchers.IO)
    }

    override suspend fun getChatRoomByUid(uid: String): ChatRoomLocalData {
        return chatRoomDao.getChatRoomByUid(uid).toModel()
    }

    override fun getMyUidFromLogInData(): String? {
        return auth.currentUser?.uid
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

    override suspend fun getChatRoomListFromLocal(): Flow<PagingData<ChatRoomDataWithRelatedUsers>> {
        return withContext(Dispatchers.IO) {
            val relatedUsers = getRelatedUsersForChatRoomList()
            Pager(
                config = PagingConfig(pageSize = 10, enablePlaceholders = false),
                pagingSourceFactory = {
                    chatRoomDao.getChatRoomsWithPaging()
                }
            ).flow.map { value ->
                value.map { chatRoom ->
                    ChatRoomDataWithRelatedUsers(
                        chatRoomLocalData = chatRoom.toModel(),
                        relatedUserLocalData = relatedUsers.find { chatRoom.receiver == it.uid }!!
                    )
                }
            }
        }
    }

    private fun getRelatedUsersForChatRoomList(): List<RelatedUserLocalData> {
        return userDao.getRelatedUsers().map { it.toRelationLocalData() }
    }

    override fun getRelatedUserListFromLocal(): Flow<List<UserEntity>> {
        return userDao.getAllRelatedUsersAsFlow().flowOn(Dispatchers.IO)
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
                setMyUserInformation(userData)
                trySend(true)
            }.addOnFailureListener {
                trySend(false)
            }
        awaitClose()
    }

    override suspend fun updateRelatedUserInfoWithUserEntity(userEntity: UserEntity) {
        val remoteData = getUserInfoFromRemote(userEntity.uid).first()
        if (remoteData != userEntity.toModel()) {
            userDao.updateUser(
                userEntity.copy(
                    name = remoteData!!.name.ifBlank { userEntity.name },
                    status = remoteData.status.ifBlank { userEntity.status },
                    picture = remoteData.picture.ifBlank { userEntity.picture }
                )
            )
        }
    }

    override suspend fun updateUserInfoByUid(uid: String) {
        val localEntity = getUserEntityFromLocalByUid(uid)
        val remoteData = getUserInfoFromRemote(uid).first()
        if (localEntity.toModel() != remoteData) {
            userDao.updateUser(
                localEntity.copy(
                    name = remoteData!!.name.ifBlank { localEntity.name },
                    status = remoteData.status.ifBlank { localEntity.status },
                    picture = remoteData.picture.ifBlank { localEntity.picture }
                )
            )
        }
    }

    private suspend fun getUserEntityFromLocalByUid(uid: String): UserEntity {
        return userDao.getUserInfoByUid(uid).first()
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

    override suspend fun resetMyUserData() {
        preferenceUtil.setMyData(UserData())
    }

    override suspend fun resetFriendData() {
        userDao.resetUserDatabase()
    }

    override suspend fun resetChatRoomData() {
        chatRoomDao.resetChatRoomDatabase()
    }

    override fun setMyUserInformation(userData: UserData) {
        preferenceUtil.setMyData(userData)
    }

    override suspend fun deleteFriendRequest(relatedUserLocalData: RelatedUserLocalData): ChangeRelationResult {
        return try {
            val relation = UserRelationState.UNKNOWN
            val remoteRequest = changeRelationRemote(relatedUserLocalData, relation).first()
            if (remoteRequest) {
                val localRequest = changeRelationLocal(relatedUserLocalData, relation).first()
                if (localRequest == ChangeRelationResult.SUCCESS) {
                    ChangeRelationResult.SUCCESS
                } else {
                    ChangeRelationResult.FAILURE
                }
            } else {
                ChangeRelationResult.FAILURE
            }
        } catch (e: Exception) {
            ChangeRelationResult.FAILURE
        }
    }

    override suspend fun changeRelationLocal(
        relatedUserLocalData: RelatedUserLocalData,
        relation: UserRelationState
    ): Flow<ChangeRelationResult> =
        callbackFlow {
            try {
                userDao.updateUser(
                    relatedUserLocalData.toEntity().copy(relation = relation)
                )
                trySend(ChangeRelationResult.SUCCESS)
            } catch (e: Exception) {
                trySend(ChangeRelationResult.FAILURE)
            }
            awaitClose()
        }

    override suspend fun changeRelationRemote(
        relatedUserLocalData: RelatedUserLocalData,
        relation: UserRelationState
    ): Flow<Boolean> =
        callbackFlow {
            auth.currentUser?.uid?.let {
                database.reference.child("relations").child(it).child(relatedUserLocalData.uid)
                    .setValue(
                        relatedUserLocalData.toRemoteData().copy(
                            userRelation = relation
                        )
                    ).addOnSuccessListener {
                        trySend(true)
                    }.addOnFailureListener {
                        trySend(false)
                    }
            }
            awaitClose()
        }

    override suspend fun hideFriendRequest(relatedUserLocalData: RelatedUserLocalData): ChangeRelationResult {
        return try {
            val relation = UserRelationState.HIDE
            val remoteRequest = changeRelationRemote(relatedUserLocalData, relation).first()
            if (remoteRequest) {
                val localRequest = changeRelationLocal(relatedUserLocalData, relation).first()
                if (localRequest == ChangeRelationResult.SUCCESS) {
                    ChangeRelationResult.SUCCESS
                } else {
                    ChangeRelationResult.FAILURE
                }
            } else {
                ChangeRelationResult.FAILURE
            }
        } catch (e: Exception) {
            ChangeRelationResult.FAILURE
        }
    }

    override suspend fun blockFriendRequest(relatedUserLocalData: RelatedUserLocalData): ChangeRelationResult {
        return try {
            val relation = UserRelationState.BLOCKED
            val remoteRequest = changeRelationRemote(relatedUserLocalData, relation).first()
            if (remoteRequest) {
                val localRequest = changeRelationLocal(relatedUserLocalData, relation).first()
                if (localRequest == ChangeRelationResult.SUCCESS) {
                    ChangeRelationResult.SUCCESS
                } else {
                    ChangeRelationResult.FAILURE
                }
            } else {
                ChangeRelationResult.FAILURE
            }
        } catch (e: Exception) {
            ChangeRelationResult.FAILURE
        }
    }
}
