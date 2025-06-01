package com.gyleedev.domain.usecase

import com.gyleedev.chatchat.domain.model.MessageData
import com.gyleedev.chatchat.domain.repository.MessageRepository
import javax.inject.Inject

class InsertMessageToLocalUseCase @Inject constructor(
    private val repository: MessageRepository
) {
    suspend operator fun invoke(messageData: MessageData, rid: Long) =
        repository.insertMessageToLocal(messageData, rid)
}
