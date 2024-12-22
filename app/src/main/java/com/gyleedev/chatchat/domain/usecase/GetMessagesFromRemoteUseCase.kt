package com.gyleedev.chatchat.domain.usecase

import com.gyleedev.chatchat.data.repository.UserRepository
import com.gyleedev.chatchat.domain.ChatRoomLocalData
import com.gyleedev.chatchat.domain.MessageData
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMessagesFromRemoteUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(chatRoom: ChatRoomLocalData): Flow<MessageData?> {
        return repository.getMessageListener(chatRoom)
    }
}
