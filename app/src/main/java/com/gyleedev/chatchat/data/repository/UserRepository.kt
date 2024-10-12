package com.gyleedev.chatchat.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.gyleedev.chatchat.data.database.FriendDao
import com.gyleedev.chatchat.data.database.toEntity
import com.gyleedev.chatchat.data.database.toModel
import com.gyleedev.chatchat.domain.LogInResult
import com.gyleedev.chatchat.domain.SignInResult
import com.gyleedev.chatchat.domain.UserData
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

interface UserRepository {
    fun getUsersFromDatabase(): List<UserData>
    suspend fun signInUser(id: String, password: String): Flow<UserData?>
    suspend fun logInRequest(id: String, password: String): Flow<LogInResult>
    suspend fun searchUser(email: String): Flow<UserData?>
    fun fetchUserExists(): Boolean
    suspend fun writeUserToRealtimeDatabase(user: UserData): Flow<SignInResult>
    suspend fun getMyUserInformation(): Flow<UserData?>
    suspend fun addFriend(user: UserData): Flow<Boolean>
    suspend fun getMyFriendList(): Flow<List<UserData?>?>
}

class UserRepositoryImpl @Inject constructor(
    private val friendDao: FriendDao,
    firebase: Firebase,
    private val auth: FirebaseAuth,
    private val firebaseStorage: FirebaseStorage
) : UserRepository {
    val database =
        firebase.database("https://chat-a332d-default-rtdb.asia-southeast1.firebasedatabase.app/")

    override fun getUsersFromDatabase(): List<UserData> {
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

    override suspend fun writeUserToRealtimeDatabase(user: UserData): Flow<SignInResult> =
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

    private fun writeUserToRoomDatabase(user: UserData) {
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

    override suspend fun addFriend(user: UserData): Flow<Boolean> = callbackFlow {
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

    override suspend fun getMyFriendList(): Flow<List<UserData?>?> = callbackFlow {
        auth.currentUser?.let {
            database.reference.child("friends").child(it.uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val friendList = mutableListOf<UserData?>()
                        for (ds in snapshot.getChildren()) {
                            val snap = ds.getValue(UserData::class.java)
                            if (snap != null) {
                                friendList.add(snap)
                            }
                        }
                        println(friendList)
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
}
