package com.gyleedev.domain.usecase

import com.gyleedev.chatchat.domain.model.ChatRoomLocalData
import com.gyleedev.chatchat.domain.model.MessageData
import com.gyleedev.chatchat.domain.model.UserRelationState
import com.gyleedev.chatchat.domain.repository.MessageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMessagesFromRemoteUseCase @Inject constructor(
    private val repository: MessageRepository
) {
    operator fun invoke(
        chatRoom: ChatRoomLocalData,
        userRelationState: UserRelationState
    ): Flow<MessageData?> {
        return repository.getMessageListener(chatRoom, userRelationState)
    }
}
