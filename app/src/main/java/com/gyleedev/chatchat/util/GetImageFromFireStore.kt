package com.gyleedev.chatchat.util

import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

fun getImageFromFireStore(fileName: String): Flow<String> = callbackFlow {
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
