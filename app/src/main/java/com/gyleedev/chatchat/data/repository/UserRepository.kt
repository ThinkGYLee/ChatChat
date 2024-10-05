package com.gyleedev.chatchat.data.repository

import android.content.ContentValues.TAG
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.gyleedev.chatchat.data.database.UserDao
import com.gyleedev.chatchat.data.database.toModel
import com.gyleedev.chatchat.domain.UserData
import javax.inject.Inject

interface UserRepository {
    fun getUsersFromDatabase(): List<UserData>
    fun signInUser(id: String, password: String)
    fun logInRequest(id: String, password: String)
    fun searchUser(email: String)
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

    override fun signInUser(id: String, password: String) {
        auth.createUserWithEmailAndPassword(id, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success")
                    val user = auth.currentUser
                    println(user)
                    user?.uid?.let { writeUserToDatabase(id, it) }
                    // updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)

                    println("Authentication failed. :${task.exception?.message}")
                    // updateUI(null)
                }
            }
    }

    private fun writeUserToDatabase(email: String, uid: String) {
        val user = UserData(email = email, name = "Anonymous User", uid = uid)
        database.reference.child(
            "users"
        ).child(uid).setValue(user)
    }

    override fun logInRequest(id: String, password: String) {
        auth.signInWithEmailAndPassword(id, password)
            .addOnCompleteListener() { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithEmail:success")
                    println("success")
                    val user = auth.currentUser
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    println("fail : ${task.exception}")
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
}
