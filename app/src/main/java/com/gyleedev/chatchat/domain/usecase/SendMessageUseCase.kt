package com.gyleedev.chatchat.domain.usecase

import com.gyleedev.chatchat.data.repository.MessageRepository
import com.gyleedev.chatchat.domain.MessageData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SendMessageUseCase @Inject constructor(
    private val repository: MessageRepository
) {
    suspend operator fun invoke(messageData: MessageData, rid: Long) {
        return withContext(Dispatchers.IO) {
            val messageId = repository.insertMessageToLocal(messageData, rid)
            if (messageId != null) {
                val request = repository.insertMessageToRemote(messageData).first()
                val message = messageData.copy(messageSendState = request)

                repository.updateMessageState(messageId, rid, message)
            }
        }
    }
}
