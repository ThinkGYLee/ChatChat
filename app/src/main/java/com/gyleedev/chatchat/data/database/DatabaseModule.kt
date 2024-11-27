package com.gyleedev.chatchat.data.database

import android.content.Context
import androidx.room.Room
import com.gyleedev.chatchat.data.database.dao.ChatRoomDao
import com.gyleedev.chatchat.data.database.dao.FriendDao
import com.gyleedev.chatchat.data.database.dao.MessageDao
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
    fun providesFriendDao(chatDataDatabase: ChatDataDatabase): FriendDao = chatDataDatabase.friendDao()

    @Singleton
    @Provides
    fun providesChatRoomDao(chatDataDatabase: ChatDataDatabase): ChatRoomDao = chatDataDatabase.chatRoomDao()

    @Singleton
    @Provides
    fun providesMessageDao(chatDataDatabase: ChatDataDatabase): MessageDao = chatDataDatabase.messageDao()
}
