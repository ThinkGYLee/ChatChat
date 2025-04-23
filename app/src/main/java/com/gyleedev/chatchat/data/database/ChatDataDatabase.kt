package com.gyleedev.chatchat.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.gyleedev.chatchat.data.database.dao.ChatListWithMessageAndFriendDao
import com.gyleedev.chatchat.data.database.dao.ChatRoomDao
import com.gyleedev.chatchat.data.database.dao.FavoriteDao
import com.gyleedev.chatchat.data.database.dao.MessageDao
import com.gyleedev.chatchat.data.database.dao.UserAndFavoriteDao
import com.gyleedev.chatchat.data.database.dao.UserDao
import com.gyleedev.chatchat.data.database.entity.ChatRoomEntity
import com.gyleedev.chatchat.data.database.entity.FavoriteEntity
import com.gyleedev.chatchat.data.database.entity.MessageEntity
import com.gyleedev.chatchat.data.database.entity.UserEntity
import com.gyleedev.chatchat.data.database.entity.UserFts

@Database(
    entities = [
        UserEntity::class,
        UserFts::class,
        FavoriteEntity::class,
        ChatRoomEntity::class,
        MessageEntity::class
    ],
    version = 1,
    exportSchema = true
)

abstract class ChatDataDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun chatRoomDao(): ChatRoomDao
    abstract fun messageDao(): MessageDao
    abstract fun chatListWithMessageAndFriendDao(): ChatListWithMessageAndFriendDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun userAndFavoriteDao(): UserAndFavoriteDao
}
