package com.gyleedev.chatchat.data.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import com.gyleedev.chatchat.data.database.entity.MessageEntity

@Dao
interface ChatListWithMessageAndFriendDao {

    @Query("SELECT * FROM chatroom INNER JOIN friend ON chatroom.receiver = friend.uid INNER JOIN message ON message.rid = chatroom.rid ORDER BY time DESC LIMIT 1")
    fun getChatListFeed(id: String): PagingSource<Int, MessageEntity>
}
