package com.gyleedev.domain.usecase

import com.gyleedev.domain.repository.MessageRepository
import javax.inject.Inject

class MessageResetUseCase @Inject constructor(
    private val repository: MessageRepository,
) {
    suspend operator fun invoke() {
        repository.resetMessageData()
    }
}
