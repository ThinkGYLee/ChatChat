package com.gyleedev.chatchat.data.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.gyleedev.chatchat.data.database.entity.ChatRoomEntity
import com.gyleedev.chatchat.data.database.entity.FriendEntity

@Dao
interface ChatRoomDao {

    @Query("SELECT * FROM friend")
    fun getUsersWithPaging(): PagingSource<Int, FriendEntity>

    @Insert
    fun insertChatRoom(chatRoom: ChatRoomEntity): Long

    @Query("SELECT * FROM chatroom WHERE receiver = :receiver")
    fun getChatRoomByUid(receiver: String): ChatRoomEntity

    @Update
    fun updateChatRoom(chatRoom: ChatRoomEntity)

    @Query("SELECT * FROM chatroom")
    fun getChatRoomsWithPaging(): PagingSource<Int, ChatRoomEntity>
}
