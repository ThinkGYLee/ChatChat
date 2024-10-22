package com.gyleedev.chatchat.data.database

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Singleton
    @Provides
    fun providesUserDatabase(@ApplicationContext context: Context): ChatDataDatabase {
        return Room.databaseBuilder(
            context,
            ChatDataDatabase::class.java,
            "database"
        ).build()
    }

    @Singleton
    @Provides
    fun providesUserDao(chatDataDatabase: ChatDataDatabase): FriendDao = chatDataDatabase.userDao()
}
