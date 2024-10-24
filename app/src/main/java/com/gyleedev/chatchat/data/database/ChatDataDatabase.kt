package com.gyleedev.chatchat.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.gyleedev.chatchat.data.database.dao.ChatRoomDao
import com.gyleedev.chatchat.data.database.dao.FriendDao
import com.gyleedev.chatchat.data.database.dao.MessageDao
import com.gyleedev.chatchat.data.database.entity.ChatRoomEntity
import com.gyleedev.chatchat.data.database.entity.FriendEntity
import com.gyleedev.chatchat.data.database.entity.MessageEntity

@Database(
    entities = [
        FriendEntity::class,
        ChatRoomEntity::class,
        MessageEntity::class
    ],
    version = 1,
    exportSchema = true
)

abstract class ChatDataDatabase : RoomDatabase() {
    abstract fun friendDao(): FriendDao
    abstract fun chatRoomDao(): ChatRoomDao
    abstract fun messageDao(): MessageDao
}
