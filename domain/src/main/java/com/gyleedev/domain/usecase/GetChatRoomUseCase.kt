package com.gyleedev.domain.usecase

import com.gyleedev.domain.model.ChatCreationState
import com.gyleedev.domain.model.RelatedUserLocalData
import com.gyleedev.domain.repository.ChatRoomRepository
import javax.inject.Inject

class GetChatRoomUseCase @Inject constructor(
    private val repository: ChatRoomRepository
) {
    operator fun invoke(
        user: RelatedUserLocalData,
        chatCreationState: ChatCreationState = ChatCreationState.CheckAndGetDataFromLocal
    ) = repository.getChatRoom(user, chatCreationState)
}
