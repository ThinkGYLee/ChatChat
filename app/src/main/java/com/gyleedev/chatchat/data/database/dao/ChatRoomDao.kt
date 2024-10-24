package com.gyleedev.chatchat.data.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.gyleedev.chatchat.data.database.entity.ChatRoomEntity
import com.gyleedev.chatchat.data.database.entity.FriendEntity

@Dao
interface ChatRoomDao {

    @Query("SELECT * FROM friend")
    fun getUsersWithPaging(): PagingSource<Int, FriendEntity>

    @Insert
    fun insertChatRoom(chatRoom: ChatRoomEntity): Long
}
