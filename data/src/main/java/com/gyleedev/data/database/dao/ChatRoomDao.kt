package com.gyleedev.data.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.gyleedev.data.database.entity.ChatRoomEntity

@Dao
interface ChatRoomDao {

    @Insert
    fun insertChatRoom(chatRoom: ChatRoomEntity): Long

    @Query("SELECT * FROM chatroom WHERE receiver = :receiver")
    fun getChatRoomByUid(receiver: String): ChatRoomEntity?

    @Query("SELECT * FROM chatroom WHERE rid = :rid")
    fun getChatRoomByRid(rid: String): ChatRoomEntity?

    @Update
    fun updateChatRoom(chatRoom: ChatRoomEntity)

    @Query("SELECT * FROM chatroom")
    fun getChatRoomsWithPaging(): PagingSource<Int, ChatRoomEntity>

    @Query("DELETE FROM chatroom")
    suspend fun resetChatRoomDatabase()
}
