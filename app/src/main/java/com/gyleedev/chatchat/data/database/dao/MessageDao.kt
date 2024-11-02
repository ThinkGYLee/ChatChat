package com.gyleedev.chatchat.data.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.gyleedev.chatchat.data.database.entity.MessageEntity

@Dao
interface MessageDao {
    @Insert
    fun insertChatRoom(message: MessageEntity): Long

    @Query("SELECT * FROM message WHERE rid = :id")
    fun getUsersWithPaging(id: String): PagingSource<Int, MessageEntity>

    @Update
    fun updateMessageState(message: MessageEntity)
}
