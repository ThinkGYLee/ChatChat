package com.gyleedev.chatchat.domain.usecase

import com.gyleedev.chatchat.data.repository.MessageRepository
import com.gyleedev.chatchat.domain.ChatRoomLocalData
import com.gyleedev.chatchat.domain.MessageData
import com.gyleedev.chatchat.domain.UserRelationState
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMessagesFromRemoteUseCase @Inject constructor(
    private val repository: MessageRepository
) {
    operator fun invoke(chatRoom: ChatRoomLocalData, userRelationState: UserRelationState): Flow<MessageData?> {
        return repository.getMessageListener(chatRoom, userRelationState)
    }
}
