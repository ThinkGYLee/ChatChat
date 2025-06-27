package com.gyleedev.domain.usecase

import com.gyleedev.domain.model.ChatRoomAndReceiverLocalData
import com.gyleedev.domain.model.MessageData
import com.gyleedev.domain.model.UserRelationState
import com.gyleedev.domain.repository.MessageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMessagesFromRemoteUseCase @Inject constructor(
    private val repository: MessageRepository
) {
    operator fun invoke(
        chatRoom: ChatRoomAndReceiverLocalData,
        userRelationState: UserRelationState
    ): Flow<MessageData?> {
        return repository.getMessageListener(chatRoom, userRelationState)
    }
}
