package com.gyleedev.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import com.gyleedev.data.database.entity.ReceiverEntity

@Dao
interface ReceiverDao {

    @Insert
    fun insertReceiver(receiver: ReceiverEntity): Long

    @Insert
    fun insertReceivers(list: List<ReceiverEntity>)
}
