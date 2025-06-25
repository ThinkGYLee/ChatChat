package com.gyleedev.data.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.gyleedev.data.database.entity.ChatRoomAndReceiverEntity
import com.gyleedev.data.database.entity.ChatRoomEntity

@Dao
interface ChatRoomAndReceiverDao {

    @Insert
    fun insertChatRoom(chatRoom: ChatRoomEntity): Long

    @Transaction
    @Query("""
        SELECT * FROM chatroom
        WHERE id IN (
            SELECT chatroom_entity_id FROM receiver WHERE receiver = :receiver
        )
    """)
    fun getChatRoomByUid(receiver: String): ChatRoomAndReceiverEntity?

    @Update
    fun updateChatRoom(chatRoom: ChatRoomEntity)

    @Query("SELECT * FROM chatroom")
    fun getChatRoomsWithPaging(): PagingSource<Int, ChatRoomEntity>

    @Query("DELETE FROM chatroom")
    suspend fun resetChatRoomDatabase()
}
