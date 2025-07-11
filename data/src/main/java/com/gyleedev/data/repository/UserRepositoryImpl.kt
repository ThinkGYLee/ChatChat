package com.gyleedev.data.repository

import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.gyleedev.data.database.dao.FavoriteDao
import com.gyleedev.data.database.dao.UserAndFavoriteDao
import com.gyleedev.data.database.dao.UserDao
import com.gyleedev.data.database.entity.FavoriteEntity
import com.gyleedev.data.database.entity.UserEntity
import com.gyleedev.data.database.entity.toEntity
import com.gyleedev.data.database.entity.toEntityAsFriend
import com.gyleedev.data.database.entity.toLocalData
import com.gyleedev.data.database.entity.toModel
import com.gyleedev.data.database.entity.toRelationLocalData
import com.gyleedev.data.preference.AuthenticationPreference
import com.gyleedev.data.preference.MyDataPreference
import com.gyleedev.domain.model.BlockedUser
import com.gyleedev.domain.model.ChangeRelationResult
import com.gyleedev.domain.model.LogInResult
import com.gyleedev.domain.model.ProcessResult
import com.gyleedev.domain.model.RelatedUserLocalData
import com.gyleedev.domain.model.RelatedUserRemoteData
import com.gyleedev.domain.model.SearchUserResult
import com.gyleedev.domain.model.SignInResult
import com.gyleedev.domain.model.UserData
import com.gyleedev.domain.model.UserRelationState
import com.gyleedev.domain.model.UserState
import com.gyleedev.domain.model.VerifiedState
import com.gyleedev.domain.model.toBlockedUser
import com.gyleedev.domain.model.toRelatedUserLocalData
import com.gyleedev.domain.model.toRemoteData
import com.gyleedev.domain.repository.UserRepository
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

class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val database: FirebaseDatabase,
    storage: FirebaseStorage,
    private val auth: FirebaseAuth,
    private val favoriteDao: FavoriteDao,
    private val userAndFavoriteDao: UserAndFavoriteDao,
    private val myDataPreference: MyDataPreference,
    private val authenticationPreference: AuthenticationPreference
) : UserRepository {

    private val imageStorageReference = storage.getReference("image")

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
                status = " ",
                verified = false
            )
            setMyUserInformation(myData)
            authenticationPreference.setPassword(password)
            authenticationPreference.setState(VerifiedState.NOTINPROGRESS)
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

    // 유저 검색 플로우
    // 내가 상대에게 블락됐는지 확인
    // 문제 없으면 유저 정보 가져와서 리턴
    // 문제 있으면 null 리턴
    override suspend fun searchUserRequest(email: String): Flow<SearchUserResult> =
        callbackFlow {
            val myData = getMyUserDataFromPreference()
            if (email == myData.email) {
                trySend(SearchUserResult.Failure(message = "this is my email"))
            }
            val checkBlockState = checkUserBlockState(email).first()
            if (checkBlockState == ProcessResult.Failure) {
                val result = searchUser(email).first()
                if (result != null) {
                    trySend(SearchUserResult.Success(user = result))
                } else {
                    trySend(SearchUserResult.Failure(message = "no such user"))
                }
            } else {
                trySend(SearchUserResult.Failure(message = "no such user"))
            }
            awaitClose()
        }

    // 리모트에서 유저 검색 기능
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

    override fun fetchUserState(): UserState {
        val authState = auth.currentUser != null
        return if (authState) {
            val verified = getMyUserDataFromPreference().verified
            if (verified) {
                UserState.Verified
            } else {
                UserState.UnVerified
            }
        } else {
            UserState.NoUser
        }
    }

    override suspend fun getMyDataFromRemote(): Flow<UserData?> = callbackFlow {
        val myData = myDataPreference.getMyData()
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

    override fun getMyUserDataFromPreference(): UserData {
        return myDataPreference.getMyData()
    }

    override suspend fun addRelatedUserToRemote(
        user: UserData,
        userRelation: UserRelationState
    ): Flow<Boolean> = callbackFlow {
        val relation = RelatedUserRemoteData(
            uid = user.uid,
            email = user.email,
            name = user.name,
            picture = user.picture,
            status = user.status,
            userRelation = userRelation,
            favoriteState = false,
            verified = user.verified
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

    // 리모트에서 가져온 관게 리스트를 로컬에 인서트 할 때 사용
    // list의 아이템별로 로컬에 존재하는 user인지 확인 후에 인서트
    // 존재하지 않는 유저만 인서트 하기 때문에 favorite 도 함께 생성해서 인서트
    // Favorite 개수는 count로 먼저 가져와서 user의 favorite state가 true 일때만 숫자++
    override suspend fun insertMyRelationsToLocal(list: List<RelatedUserRemoteData>) {
        withContext(Dispatchers.IO) {
            var count = getFavoriteCount()
            list.forEach {
                val localUser = getUserEntityFromLocalByUid(it.uid)
                if (localUser == null) {
                    val id = insertUserToLocal(it)
                    insertFavoriteToLocal(it, id, count.toLong())
                    if (it.favoriteState) count++
                }
            }
        }
    }

    private fun insertUserToLocal(user: RelatedUserRemoteData): Long {
        return userDao.insertUser(user.toRelatedUserLocalData().toEntity())
    }

    private fun getFavoriteCount(): Int {
        return favoriteDao.getFavoriteCount()
    }

    private fun insertFavoriteToLocal(user: RelatedUserRemoteData, id: Long, count: Long) {
        val favorite = FavoriteEntity(
            userEntityId = id,
            favoriteState = user.favoriteState,
            favoriteNumber = if (user.favoriteState) count + 1L else null
        )
        favoriteDao.insertFavorite(favorite)
    }

    // 검색한 사람을 친구 추가해서 리모트에 올리고 인서트할 때 사용
    // 내 로컬에 없는 사람을 인서트 하기 때문에 favorite도 함께 인서트한다
    // 새로 인서트하기 때문에 favorite 관련 항목은 모두 false와 null로
    override suspend fun insertFriendToLocal(user: UserData): Flow<Boolean> = callbackFlow {
        try {
            val id = userDao.insertUser(user.toEntityAsFriend())
            val favorite = FavoriteEntity(
                favoriteState = false,
                favoriteNumber = null,
                userEntityId = id
            )
            favoriteDao.insertFavorite(favorite)
            trySend(true)
        } catch (e: Exception) {
            trySend(false)
        }
        awaitClose()
    }

    override fun getFriends(): Flow<PagingData<RelatedUserLocalData>> {
        return Pager(
            config = PagingConfig(pageSize = 10, enablePlaceholders = false),
            pagingSourceFactory = {
                userDao.getFriendsPaging()
            }
        ).flow.map { value ->
            value.map { it.toRelationLocalData() }
        }.flowOn(Dispatchers.IO)
    }

    override fun getFavorites(): Flow<PagingData<RelatedUserLocalData>> {
        return Pager(
            config = PagingConfig(pageSize = 10, enablePlaceholders = false),
            pagingSourceFactory = {
                userAndFavoriteDao.getFavoritesPaging()
            }
        ).flow.map { value ->
            value.map { it.toLocalData() }
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun getFriendsCount(): Long {
        return userDao.getFriendsCount()
    }

    override fun getFriendById(uid: String): Flow<RelatedUserLocalData> {
        return userDao.getUserInfoByUid(uid).map { requireNotNull(it).toRelationLocalData() }
            .flowOn(Dispatchers.IO)
    }

    override suspend fun getFriendAndFavoriteByUid(uid: String): RelatedUserLocalData {
        return withContext(Dispatchers.IO) {
            val localData = userAndFavoriteDao.getUserAndFavoriteByUid(uid).first()
            return@withContext if (localData == null) {
                val remoteData = getUserInfoFromRemote(uid).first()
                val notNullRemoteData = requireNotNull(remoteData)
                val relatedUserRemoteData = RelatedUserRemoteData(
                    name = notNullRemoteData.picture,
                    status = notNullRemoteData.status,
                    verified = notNullRemoteData.verified,
                    picture = notNullRemoteData.picture,
                    favoriteState = false,
                    userRelation = UserRelationState.UNKNOWN,
                    email = notNullRemoteData.email,
                    uid = notNullRemoteData.uid
                )
                val id = insertUserToLocal(
                    relatedUserRemoteData
                )
                insertFavoriteToLocal(
                    user = relatedUserRemoteData,
                    id = id,
                    count = getFavoriteCount().toLong()
                )
                RelatedUserLocalData(
                    id = id,
                    name = requireNotNull(remoteData).picture,
                    status = remoteData.status,
                    verified = remoteData.verified,
                    picture = remoteData.picture,
                    favoriteState = false,
                    favoriteNumber = null,
                    userRelation = UserRelationState.UNKNOWN,
                    email = requireNotNull(remoteData).email,
                    uid = requireNotNull(remoteData).uid
                )
            } else {
                println("else")
                println(localData)
                return@withContext localData.toLocalData()
            }
        }
    }

    override fun getMyUidFromLogInData(): String? {
        return auth.currentUser?.uid
    }

    override fun getRelatedUsersForChatRoomList(): List<RelatedUserLocalData> {
        return userDao.getRelatedUsers().map { it.toRelationLocalData() }
    }

    override fun getRelatedUserListFromLocal(): Flow<List<RelatedUserLocalData>> {
        return userDao.getAllRelatedUsersAsFlow().map { list ->
            list.map { entity ->
                entity.toRelationLocalData()
            }
        }.flowOn(Dispatchers.IO)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun updateMyUserInfo(user: UserData): Flow<Boolean> = callbackFlow {
        val imagePath = uploadAndGetImage(user.picture.toUri()).first()
        val userData = UserData(
            email = user.email,
            name = user.name,
            status = user.status,
            uid = user.uid,
            picture = imagePath,
            verified = user.verified
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

    override suspend fun updateRelatedUserInfoWithUserEntity(relatedUserLocalData: RelatedUserLocalData) {
        val entity = relatedUserLocalData.toEntity()
        val remoteData = getUserInfoFromRemote(entity.uid).first()
        if (remoteData != entity.toModel()) {
            userDao.updateUser(
                entity.copy(
                    name = remoteData!!.name.ifBlank { entity.name },
                    status = remoteData.status.ifBlank { entity.status },
                    picture = remoteData.picture.ifBlank { entity.picture }
                )
            )
        }
    }

    override suspend fun updateUserInfoByUid(uid: String) {
        val localEntity = getUserEntityFromLocalByUid(uid)
        val remoteData = getUserInfoFromRemote(uid).first()
        if (requireNotNull(localEntity).toModel() != remoteData) {
            userDao.updateUser(
                localEntity.copy(
                    name = remoteData!!.name.ifBlank { localEntity.name },
                    status = remoteData.status.ifBlank { localEntity.status },
                    picture = remoteData.picture
                )
            )
        }
    }

    private suspend fun getUserEntityFromLocalByUid(uid: String): UserEntity? {
        return userDao.getUserInfoByUid(uid).first()
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

    override suspend fun logoutRequest() {
        auth.signOut()
    }

    override suspend fun resetMyUserData() {
        myDataPreference.setMyData(UserData())
    }

    override suspend fun resetFriendData() {
        userDao.resetUserDatabase()
    }

    override fun setMyUserInformation(userData: UserData) {
        myDataPreference.setMyData(userData)
    }

    override suspend fun deleteFriendRequest(relatedUserLocalData: RelatedUserLocalData): ChangeRelationResult {
        return try {
            val relatedUser = relatedUserLocalData.copy(
                userRelation = UserRelationState.UNKNOWN,
                favoriteState = false
            )
            val remoteRequest = changeRelatedUserRemote(relatedUser).first()
            if (remoteRequest == ProcessResult.Success) {
                val localRequest = changeRelatedUserLocal(relatedUser).first()
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

    override suspend fun changeRelatedUserLocal(
        relatedUserLocalData: RelatedUserLocalData
    ): Flow<ChangeRelationResult> =
        callbackFlow {
            try {
                userDao.updateUser(relatedUserLocalData.toEntity())
                if (!relatedUserLocalData.favoriteState) {
                    updateLocalFavoriteWithRelatedUserLocalData(relatedUserLocalData)
                }
                trySend(ChangeRelationResult.SUCCESS)
            } catch (e: Exception) {
                trySend(ChangeRelationResult.FAILURE)
            }
            awaitClose()
        }

    override suspend fun changeRelatedUserRemote(
        relatedUserLocalData: RelatedUserLocalData
    ): Flow<ProcessResult> =
        callbackFlow {
            auth.currentUser?.uid?.let {
                database.reference.child("relations").child(it).child(relatedUserLocalData.uid)
                    .setValue(
                        relatedUserLocalData.toRemoteData()
                    ).addOnSuccessListener {
                        trySend(ProcessResult.Success)
                    }.addOnFailureListener {
                        trySend(ProcessResult.Failure)
                    }
            }
            awaitClose()
        }

    override suspend fun hideFriendRequest(relatedUserLocalData: RelatedUserLocalData): ChangeRelationResult {
        return try {
            val relatedUser = relatedUserLocalData.copy(
                userRelation = UserRelationState.HIDE,
                favoriteState = false
            )
            val remoteRequest = changeRelatedUserRemote(relatedUser).first()
            if (remoteRequest == ProcessResult.Success) {
                val localRequest = changeRelatedUserLocal(relatedUser).first()
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

    override suspend fun blockRelatedUserRequest(relatedUserLocalData: RelatedUserLocalData): ChangeRelationResult {
        return try {
            val relatedUser = relatedUserLocalData.copy(
                userRelation = UserRelationState.BLOCKED,
                favoriteState = false
            )
            val changeRelationRemoteRequest = changeRelatedUserRemote(relatedUser).first()
            val updateBlockStatusRemote = addUserToRemoteBlockedEntity(relatedUser).first()
            if (changeRelationRemoteRequest == ProcessResult.Success && updateBlockStatusRemote == ProcessResult.Success) {
                val localRequest = changeRelatedUserLocal(relatedUser).first()
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

    // 로컬에서 나랑 관련이 있었나 확인
    // 있었으면 그 정보 바탕으로 블락 절차
    // 없었으면 리모트에 관계에 추가, 블락드에 추가, 로컬에 추가
    override suspend fun blockUnknownUserRequest(userData: UserData): ChangeRelationResult {
        val isUserExistsLocal = getUserEntityFromLocalByUid(userData.uid)
        if (isUserExistsLocal == null) {
            val uploadData = RelatedUserRemoteData(
                uid = userData.uid,
                email = userData.email,
                status = userData.status,
                favoriteState = false,
                picture = userData.picture,
                userRelation = UserRelationState.BLOCKED,
                verified = userData.verified
            )
            val remoteAddRequest = addRelatedUserToRemote(
                user = userData,
                userRelation = UserRelationState.BLOCKED
            ).first()
            val localRequest = insertUserToLocal(uploadData)
            val addBlockRequest = addUserToRemoteBlockedEntity(
                uploadData.toRelatedUserLocalData().copy(id = localRequest)
            ).first()
            return if (remoteAddRequest && addBlockRequest == ProcessResult.Success) {
                ChangeRelationResult.SUCCESS
            } else {
                ChangeRelationResult.FAILURE
            }
        } else {
            return blockRelatedUserRequest(isUserExistsLocal.toRelationLocalData())
        }
    }

    // remote의 relations에 정보를 넣고 성공 실패로 local에 넣을지 결정
    // remote에 유저가 존재하는지는 확인할 필요 없음 있던 없던 덮어 씀
    // 유저 상태가 블락된 유저일 때 먼저 서치불가 정보를 해제가 성공하면 relations 변경으로
    override suspend fun userToFriendRequest(relatedUserLocalData: RelatedUserLocalData): ChangeRelationResult {
        try {
            val relatedUser = relatedUserLocalData.copy(userRelation = UserRelationState.FRIEND)
            if (relatedUserLocalData.userRelation == UserRelationState.BLOCKED) {
                val deleteFromBlocked =
                    deleteUserFromRemoteBlockedEntity(relatedUserLocalData).first()
                if (deleteFromBlocked == ProcessResult.Failure) {
                    return ChangeRelationResult.FAILURE
                }
            }
            val remoteChangeUserRelationRequest = changeRelatedUserRemote(relatedUser).first()
            return if (remoteChangeUserRelationRequest == ProcessResult.Success) {
                val localRequest = changeRelatedUserLocal(relatedUser).first()
                if (localRequest == ChangeRelationResult.SUCCESS) {
                    ChangeRelationResult.SUCCESS
                } else {
                    ChangeRelationResult.FAILURE
                }
            } else {
                ChangeRelationResult.FAILURE
            }
        } catch (e: Exception) {
            return ChangeRelationResult.FAILURE
        }
    }

    override fun getFriendsWithName(query: String): Flow<PagingData<RelatedUserLocalData>> {
        val searchQuery = "%$query%"
        return Pager(
            config = PagingConfig(pageSize = 10, enablePlaceholders = false),
            pagingSourceFactory = {
                userDao.getFriendsWithName(searchQuery)
            }
        ).flow.map { value ->
            value.map { entity ->
                entity.toRelationLocalData()
            }
        }
    }

    // TODO FTS 서치로 바꿀준비
    override fun getHideFriendsWithName(query: String): Flow<PagingData<RelatedUserLocalData>> {
        val searchQuery = "%$query%"
        return Pager(
            config = PagingConfig(pageSize = 10, enablePlaceholders = false),
            pagingSourceFactory = {
                userDao.getHideFriendsWithName(searchQuery)
            }
        ).flow.map { value ->
            value.map { entity ->
                entity.toRelationLocalData()
            }
        }
    }

    override fun getBlockedFriendsWithName(query: String): Flow<PagingData<RelatedUserLocalData>> {
        val searchQuery = "%$query%"
        return Pager(
            config = PagingConfig(pageSize = 10, enablePlaceholders = false),
            pagingSourceFactory = {
                userDao.getBlockedFriendsWithName(searchQuery)
            }
        ).flow.map { value ->
            value.map { entity ->
                entity.toRelationLocalData()
            }
        }
    }

    override fun getHideFriendsWithFullTextName(query: String): Flow<PagingData<RelatedUserLocalData>> {
        val searchQuery = "*$query*"
        return Pager(
            config = PagingConfig(pageSize = 10, enablePlaceholders = false),
            pagingSourceFactory = {
                userDao.getHideFriendsWithNameFullText(searchQuery)
            }
        ).flow.map { value ->
            value.map { entity ->
                entity.toRelationLocalData()
            }
        }
    }

    override fun getHideFriends(): Flow<PagingData<RelatedUserLocalData>> {
        return Pager(
            config = PagingConfig(pageSize = 10, enablePlaceholders = false),
            pagingSourceFactory = {
                userDao.getHideUsersPaging()
            }
        ).flow.map { value ->
            value.map { entity ->
                entity.toRelationLocalData()
            }
        }
    }

    override fun getBlockedFriends(): Flow<PagingData<RelatedUserLocalData>> {
        return Pager(
            config = PagingConfig(pageSize = 10, enablePlaceholders = false),
            pagingSourceFactory = {
                userDao.getBlockedUsersPaging()
            }
        ).flow.map { value ->
            value.map { entity ->
                entity.toRelationLocalData()
            }
        }
    }

    override fun updateUserAndFavorite(relatedUserLocalData: RelatedUserLocalData): Flow<Boolean> =
        callbackFlow {
            try {
                changeRelatedUserRemote(
                    relatedUserLocalData.copy(favoriteState = !relatedUserLocalData.favoriteState)
                ).first()
                changeRelatedUserLocal(
                    relatedUserLocalData.copy(favoriteState = !relatedUserLocalData.favoriteState)
                ).first()
                updateLocalFavoriteWithRelatedUserLocalData(relatedUserLocalData).first()
                trySend(true)
            } catch (e: Exception) {
                trySend(false)
            }
            awaitClose()
        }.flowOn(Dispatchers.IO)

    private fun getFavoriteByUserEntityId(userEntityId: Long): FavoriteEntity {
        return favoriteDao.getFavoriteByUserEntityId(userEntityId)
    }

    private fun updateLocalFavoriteWithRelatedUserLocalData(relatedUserEntity: RelatedUserLocalData): Flow<Boolean> =
        callbackFlow {
            try {
                val count = getFavoriteCount()
                val entity = getFavoriteByUserEntityId(relatedUserEntity.id)
                val updateFavoriteNumber =
                    if (!entity.favoriteState) {
                        count.toLong() + 1L
                    } else {
                        null
                    }

                favoriteDao.updateFavorite(
                    entity.copy(
                        favoriteState = !entity.favoriteState,
                        favoriteNumber = updateFavoriteNumber
                    )
                )
                if (updateFavoriteNumber == null) {
                    entity.favoriteNumber?.let { sortFavorite(it) }
                }
                trySend(true)
            } catch (e: Exception) {
                println(e)
                trySend(false)
            }
            awaitClose()
        }.flowOn(Dispatchers.IO)

    private fun sortFavorite(position: Long) {
        val list = favoriteDao.getFavoritesForSort(position)
        list.forEach { favoriteEntity ->
            favoriteDao.updateFavorite(
                favoriteEntity.copy(
                    favoriteNumber = requireNotNull(favoriteEntity.favoriteNumber) - 1L
                )
            )
        }
    }

    /*
    Block 관련 설명
    내가 상대를 차단하면 상대의 Blocked 테이블에 내 정보를 추가한다.
    내가 검색할 때 상대의 정보를 직접 검색하는 대신 나의 테이블만 확인하고 상대가 나를 차단했는지 여부 확인 후 정보 가져오기
    * */

    // Block 할 때 상대의 검색불가 목록에 내 정보 추가
    override fun addUserToRemoteBlockedEntity(relatedUserLocalData: RelatedUserLocalData): Flow<ProcessResult> =
        callbackFlow {
            val myData = getMyUserDataFromPreference()
            database.reference.child("blocked").child(relatedUserLocalData.uid).child(myData.uid)
                .setValue(myData.toBlockedUser())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        trySend(ProcessResult.Success)
                    } else {
                        trySend(ProcessResult.Failure)
                    }
                }
            awaitClose()
        }

    // Block을 풀 때 상대방의 검색불가 목록에서 내 정보 제거
    private fun deleteUserFromRemoteBlockedEntity(relatedUserLocalData: RelatedUserLocalData): Flow<ProcessResult> =
        callbackFlow {
            val myData = getMyUserDataFromPreference()
            database.reference.child("blocked")
                .child(relatedUserLocalData.uid).child(myData.uid).removeValue()
                .addOnSuccessListener {
                    trySend(ProcessResult.Success)
                }.addOnFailureListener {
                    trySend(ProcessResult.Failure)
                }
            awaitClose()
        }

    // user를 검색할 때 검색 대상의 block 리스트에 내 정보가 있나 확인
    private fun checkUserBlockState(email: String): Flow<ProcessResult> =
        callbackFlow {
            val myData = getMyUserDataFromPreference()
            database.reference.child("blocked").child(myData.uid).orderByChild("email")
                .equalTo(email)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.value != null) {
                            for (ds in snapshot.children) {
                                val snap = ds.getValue(BlockedUser::class.java)
                                if (snap != null) {
                                    trySend(ProcessResult.Success)
                                }
                            }
                        } else {
                            trySend(ProcessResult.Failure)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        println(error)
                        trySend(ProcessResult.Failure)
                    }
                })
            awaitClose()
        }

    override suspend fun cancelSigninRequest(): Boolean {
        val cancelResult = cancelSignin().first()
        if (cancelResult) {
            resetMyUserData()
            authenticationPreference.setPassword("default_password")
            authenticationPreference.setState(VerifiedState.NOTINPROGRESS)
        }
        return cancelResult
    }

    private fun reauthenticate(): Flow<Boolean> = callbackFlow {
        val user = auth.currentUser!!
        val credential = EmailAuthProvider
            .getCredential(
                myDataPreference.getMyData().email,
                authenticationPreference.getPassword()
            )
        user.reauthenticate(credential).addOnCompleteListener { task ->
            trySend(task.isSuccessful)
        }
        awaitClose()
    }

    private fun cancelSignin(): Flow<Boolean> = callbackFlow {
        val reauthenticateSuccess = reauthenticate().first()
        if (!reauthenticateSuccess) {
            trySend(false)
            awaitClose()
            return@callbackFlow
        }
        auth.currentUser!!.delete().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                auth.signOut()
                trySend(true)
            } else {
                trySend(false)
            }
        }
        awaitClose()
    }

    override fun checkUserVerified(): Flow<Boolean> = callbackFlow {
        requireNotNull(auth.currentUser).reload().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val check = requireNotNull(auth.currentUser).isEmailVerified
                trySend(check)
            } else {
                trySend(false)
            }
        }
        awaitClose()
    }

    override suspend fun getVerifiedState(): VerifiedState {
        return authenticationPreference.getState()
    }

    override suspend fun setVerifiedState(verifiedState: VerifiedState) {
        authenticationPreference.setState(verifiedState)
    }

    override suspend fun verifyEmailRequest(): Flow<Boolean> = callbackFlow {
        requireNotNull(auth.currentUser).sendEmailVerification()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    trySend(true)
                } else {
                    trySend(false)
                }
            }
        awaitClose()
    }

    override suspend fun getUsersByUid(list: List<String>): List<RelatedUserLocalData> {
        return list.map {
            requireNotNull(
                userDao.getUserInfoByUid(it).map {
                    it?.toRelationLocalData()
                }.first()
            )
        }
    }
}
