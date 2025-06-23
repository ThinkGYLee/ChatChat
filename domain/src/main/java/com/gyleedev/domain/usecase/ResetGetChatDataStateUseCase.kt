package com.gyleedev.domain.usecase

import com.gyleedev.domain.repository.ChatRoomRepository
import javax.inject.Inject

class ResetGetChatDataStateUseCase @Inject constructor(
    private val repository: ChatRoomRepository
) {
    suspend operator fun invoke() =
        repository.resetCurrentState()
}
