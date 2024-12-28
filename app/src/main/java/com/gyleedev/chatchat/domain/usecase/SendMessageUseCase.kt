package com.gyleedev.chatchat.domain.usecase

import com.gyleedev.chatchat.domain.MessageData
import com.gyleedev.chatchat.domain.MessageSendState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SendMessageUseCase @Inject constructor(
    private val insertMessageToLocalUseCase: InsertMessageToLocalUseCase,
    private val sendMessageToRemoteUseCase: SendMessageToRemoteUseCase,
    private val updateMessageStateUseCase: UpdateMessageStateUseCase
) {
    suspend operator fun invoke(messageData: MessageData, rid: Long, networkState: Boolean) {
        withContext(Dispatchers.IO) {
            val messageId = insertMessageToLocalUseCase(messageData, rid)
            if (networkState) {
                val request = try {
                    sendMessageToRemoteUseCase(messageData).first()
                } catch (e: Throwable) {
                    println(e)
                }
                if (request is MessageSendState) {
                    val message = messageData.copy(messageSendState = request)
                    updateMessageStateUseCase(messageId, rid, message)
                }
            } else {
                val message = messageData.copy(messageSendState = MessageSendState.FAIL)
                updateMessageStateUseCase(messageId, rid, message)
            }
        }
    }
}
