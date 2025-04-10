package com.gyleedev.chatchat.domain.usecase

import com.gyleedev.chatchat.data.repository.MessageRepository
import javax.inject.Inject

class MessageResetUseCase @Inject constructor(
    private val repository: MessageRepository
) {
    suspend operator fun invoke() {
        repository.resetMessageData()
    }
}
