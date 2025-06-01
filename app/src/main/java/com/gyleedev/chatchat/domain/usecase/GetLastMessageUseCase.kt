package com.gyleedev.chatchat.domain.usecase

import com.gyleedev.chatchat.data.database.entity.toModel
import com.gyleedev.chatchat.domain.model.ChatRoomLocalData
import com.gyleedev.chatchat.domain.repository.MessageRepository
import javax.inject.Inject

class GetLastMessageUseCase @Inject constructor(
    private val repository: MessageRepository
) {
    suspend operator fun invoke(chatRoom: ChatRoomLocalData) =
        repository.getLastMessage(chatRoom.rid)?.toModel()
}
