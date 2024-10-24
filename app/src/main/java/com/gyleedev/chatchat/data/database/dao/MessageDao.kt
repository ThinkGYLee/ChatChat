package com.gyleedev.chatchat.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import com.gyleedev.chatchat.data.database.entity.MessageEntity

@Dao
interface MessageDao {
    @Insert
    fun insertChatRoom(message: MessageEntity): Long
}
