package com.gyleedev.util

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import javax.inject.Inject

class FirebaseServerTimeHelper @Inject constructor(
    firebaseDatabase: FirebaseDatabase
) {
    private var offset: Long = 0L

    init {
        val offsetRef =
            firebaseDatabase.getReference(".info/serverTimeOffset")

        offsetRef.addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    offset = snapshot.getValue(Long::class.java) ?: 0L
                }

                override fun onCancelled(error: DatabaseError) {
                    // 에러 처리
                }
            }
        )
    }

    fun getEstimatedServerTime(): Long {
        // System.currentTimeMillis()는 클라이언트 로컬 시간(밀리초)인데, 여기에 offset을 더함으로써 UTC 기준 서버 시간을 추정할 수 있음
        return System.currentTimeMillis() + offset
    }
}
