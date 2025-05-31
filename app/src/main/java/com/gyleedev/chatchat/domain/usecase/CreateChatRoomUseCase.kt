package com.gyleedev.chatchat.domain.usecase

import com.gyleedev.chatchat.data.repository.ChatRoomRepository
import javax.inject.Inject

class CreateChatRoomUseCase @Inject constructor(
    private val repository: ChatRoomRepository
) {
    suspend operator fun invoke() = repository.createChatRoomData()
}
