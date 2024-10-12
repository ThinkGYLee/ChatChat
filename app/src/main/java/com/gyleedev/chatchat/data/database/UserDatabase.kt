package com.gyleedev.chatchat.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        FriendEntity::class
    ],
    version = 1,
    exportSchema = true
)

abstract class UserDatabase : RoomDatabase() {
    abstract fun userDao(): FriendDao
}
