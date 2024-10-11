package com.gyleedev.chatchat.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserEntity::class
    ],
    version = 1,
    exportSchema = true
)

abstract class UserDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}
