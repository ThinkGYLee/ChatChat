package com.gyleedev.data.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.gyleedev.data.database.entity.ChatRoomEntity
import com.gyleedev.data.database.entity.ReceiverEntity

@Dao
interface ReceiverDao {

    @Insert
    fun insertReceiver(receiver: ReceiverEntity): Long

    @Query("SELECT * FROM receiver WHERE receiver = :receiver")
    fun getReceiverByUid(receiver: String): ReceiverEntity?

    @Update
    fun updateReceiver(receiver: ReceiverEntity)
}
