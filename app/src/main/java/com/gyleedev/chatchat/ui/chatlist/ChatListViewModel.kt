package com.gyleedev.chatchat.ui.chatlist

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.gyleedev.chatchat.core.BaseViewModel
import com.gyleedev.chatchat.domain.UserData
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val firebase: Firebase
) : BaseViewModel() {

    fun searchUser(email: String) {
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
