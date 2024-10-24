package com.gyleedev.chatchat.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.gyleedev.chatchat.data.database.dao.FriendDao
import com.gyleedev.chatchat.data.database.entity.FriendEntity

@Database(
    entities = [
        FriendEntity::class
    ],
    version = 1,
    exportSchema = true
)

abstract class ChatDataDatabase : RoomDatabase() {
    abstract fun userDao(): FriendDao
}
