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
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

interface UserRepository {
    fun getUsersFromDatabase(): List<UserData>
    suspend fun signInUser(id: String, password: String): UserData?
    suspend fun logInRequest(id: String, password: String): LogInResult
    fun searchUser(email: String)
    fun fetchUserExists(): Boolean
    suspend fun writeUserToRealtimeDatabase(user: UserData): SignInResult
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

    override suspend fun signInUser(id: String, password: String): UserData? {
        val request = auth.createUserWithEmailAndPassword(id, password)

        return with(request.await()) {
            if (user != null) {
                UserData(email = id, name = "Anonymous User", uid = user!!.uid)
            } else {
                null
            }
        }
    }

    override suspend fun writeUserToRealtimeDatabase(user: UserData): SignInResult {
        try {
            database.reference.child(
                "users"
            ).child(user.uid).setValue(user).also { println("writedbresult: $it") }
            return SignInResult.Success
        } catch (e: Exception) {
            return SignInResult.Failure
        }
    }

    private fun writeUserToRoomDatabase(user: UserData) {
        userDao.insertUser(user.toEntity())
    }

    override suspend fun logInRequest(id: String, password: String): LogInResult {
        val request = auth.signInWithEmailAndPassword(id, password)
        return with(request.await()) {
            if (user != null) {
                LogInResult.Success
            } else {
                LogInResult.Failure("failure")
            }
        }
    }

    override fun searchUser(email: String) {
        val query =
            firebase.database("https://chat-a332d-default-rtdb.asia-southeast1.firebasedatabase.app/").reference.child(
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
