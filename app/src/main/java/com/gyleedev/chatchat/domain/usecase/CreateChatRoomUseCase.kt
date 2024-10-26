package com.gyleedev.chatchat.domain.usecase

import com.gyleedev.chatchat.data.repository.UserRepository
import com.gyleedev.chatchat.domain.ChatRoomData
import com.gyleedev.chatchat.domain.FriendData
import com.gyleedev.chatchat.domain.UserData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CreateChatRoomUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(): Flow<ChatRoomData?> {
        return withContext(Dispatchers.IO) {
            repository.createChatRoomData()
        }
    }
}
