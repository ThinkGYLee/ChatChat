package com.gyleedev.chatchat.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.gyleedev.chatchat.data.database.UserDao
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
    fun searchUser(email: String)
    fun fetchUserExists(): Boolean
    suspend fun writeUserToRealtimeDatabase(user: UserData): Flow<SignInResult>
}

class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val firebase: Firebase,
    private val auth: FirebaseAuth
) : UserRepository {
    val database =
        firebase.database("https://chat-a332d-default-rtdb.asia-southeast1.firebasedatabase.app/")

    override fun getUsersFromDatabase(): List<UserData> {
        return userDao.getUsers().map { it.toModel() }
    }

    override suspend fun signInUser(id: String, password: String): Flow<UserData?> = callbackFlow {
        auth.createUserWithEmailAndPassword(id, password).addOnSuccessListener { task ->
            trySend(UserData(email = id, name = "Anonymous User", uid = task.user!!.uid))
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
        userDao.insertUser(user.toEntity())
    }

    override suspend fun logInRequest(id: String, password: String): Flow<LogInResult> =
        callbackFlow {
            auth.signInWithEmailAndPassword(id, password).addOnSuccessListener { task ->
                trySend(LogInResult.Success)
            }.addOnFailureListener { task ->
                val message = if(task.message!= null) task.message else "unknown failure"
                trySend(LogInResult.Failure(message!!))
            }
            awaitClose()
        }

    override fun searchUser(email: String) {
        val query =
            database.reference.child(
                "users"
            ).orderByChild("email").equalTo(email)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (ds in snapshot.getChildren()) {
                    val snap = ds.getValue(UserData::class.java)
                    println(snap)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                println(error)
            }
        })
    }

    override fun fetchUserExists(): Boolean {
        return auth.currentUser != null
    }
}
