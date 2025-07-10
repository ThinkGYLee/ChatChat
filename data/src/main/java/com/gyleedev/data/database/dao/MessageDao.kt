package com.gyleedev.data.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gyleedev.data.database.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertMessage(message: MessageEntity): Long

    @Query("SELECT * FROM message WHERE rid = :id ORDER BY time DESC")
    fun getMessagesWithPaging(id: String): PagingSource<Int, MessageEntity>

    @Update
    fun updateMessageState(message: MessageEntity)

    @Query("SELECT * FROM message WHERE rid = :id ORDER BY time DESC LIMIT 1")
    fun getLastMessage(id: String): MessageEntity?

    @Query("SELECT * FROM message WHERE rid = :rid AND time = :time AND writer = :writer")
    fun getMessage(rid: String, time: Long, writer: String): Flow<MessageEntity>

    @Query("DELETE FROM message WHERE id = :messageId")
    suspend fun deleteMessage(messageId: Long)

    @Query("DELETE FROM message")
    suspend fun resetMessageDatabase()
}
