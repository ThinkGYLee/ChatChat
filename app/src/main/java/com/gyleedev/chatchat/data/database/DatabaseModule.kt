package com.gyleedev.chatchat.data.database

import android.content.Context
import androidx.room.Room
import com.gyleedev.chatchat.data.database.dao.ChatListWithMessageAndFriendDao
import com.gyleedev.chatchat.data.database.dao.ChatRoomDao
import com.gyleedev.chatchat.data.database.dao.MessageDao
import com.gyleedev.chatchat.data.database.dao.UserDao
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
    fun providesUserDao(chatDataDatabase: ChatDataDatabase): UserDao =
        chatDataDatabase.userDao()

    @Singleton
    @Provides
    fun providesChatRoomDao(chatDataDatabase: ChatDataDatabase): ChatRoomDao =
        chatDataDatabase.chatRoomDao()

    @Singleton
    @Provides
    fun providesMessageDao(chatDataDatabase: ChatDataDatabase): MessageDao =
        chatDataDatabase.messageDao()

    @Singleton
    @Provides
    fun providesChatListWithMessageAndFriendDao(chatDataDatabase: ChatDataDatabase): ChatListWithMessageAndFriendDao =
        chatDataDatabase.chatListWithMessageAndFriendDao()
}
