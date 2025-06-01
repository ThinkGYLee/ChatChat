package com.gyleedev.chatchat.domain.usecase

import com.gyleedev.chatchat.domain.repository.ChatRoomRepository
import javax.inject.Inject

class ChatRoomResetUseCase @Inject constructor(
    private val repository: ChatRoomRepository
) {
    suspend operator fun invoke() {
        repository.resetChatRoomData()
    }
}
