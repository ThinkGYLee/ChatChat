package com.gyleedev.chatchat.domain.usecase

import com.gyleedev.chatchat.data.repository.MessageRepository
import com.gyleedev.chatchat.domain.MessageData
import com.gyleedev.chatchat.domain.MessageSendState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SendMessageUseCase @Inject constructor(
    private val repository: MessageRepository
) {
    suspend operator fun invoke(messageData: MessageData, rid: Long, networkState: Boolean) {
        withContext(Dispatchers.IO) {
            val messageId = repository.insertMessageToLocal(messageData, rid)
            if (networkState) {
                val request = try {
                    repository.insertMessageToRemote(messageData).first()
                } catch (e: Throwable) {
                    println(e)
                }
                if (request is MessageSendState) {
                    val message = messageData.copy(messageSendState = request)
                    repository.updateMessageState(messageId, rid, message)
                }
            } else {
                val message = messageData.copy(messageSendState = MessageSendState.FAIL)
                repository.updateMessageState(messageId, rid, message)
            }
        }
    }
}
