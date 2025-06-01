package com.gyleedev.domain.usecase

import com.gyleedev.domain.model.ChatRoomLocalData
import com.gyleedev.domain.repository.MessageRepository
import javax.inject.Inject

class GetLastMessageUseCase @Inject constructor(
    private val repository: MessageRepository
) {
    suspend operator fun invoke(chatRoom: ChatRoomLocalData) =
        repository.getLastMessage(chatRoom.rid)
}
