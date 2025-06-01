package com.gyleedev.domain.usecase

import com.gyleedev.domain.model.MessageData
import com.gyleedev.domain.repository.MessageRepository
import javax.inject.Inject

class InsertMessageToLocalUseCase @Inject constructor(
    private val repository: MessageRepository
) {
    suspend operator fun invoke(messageData: MessageData, rid: Long) =
        repository.insertMessageToLocal(messageData, rid)
}
