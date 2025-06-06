package com.gyleedev.data.repository

import com.gyleedev.domain.repository.ChatRoomRepository
import com.gyleedev.domain.repository.MessageRepository
import com.gyleedev.domain.repository.SettingRepository
import com.gyleedev.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindsUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    @Singleton
    abstract fun bindsMessageRepository(impl: MessageRepositoryImpl): MessageRepository

    @Binds
    @Singleton
    abstract fun bindsChatRoomRepository(impl: ChatRoomRepositoryImpl): ChatRoomRepository

    @Binds
    @Singleton
    abstract fun bindsSettingRepository(impl: SettingRepositoryImpl): SettingRepository
}
