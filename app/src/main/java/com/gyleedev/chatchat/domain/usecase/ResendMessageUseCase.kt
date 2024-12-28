package com.gyleedev.chatchat.domain.usecase

import com.gyleedev.chatchat.domain.MessageData
import com.gyleedev.chatchat.domain.MessageSendState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.time.Instant
import javax.inject.Inject

class ResendMessageUseCase @Inject constructor(
    private val sendMessageToRemoteUseCase: SendMessageToRemoteUseCase,
    private val updateMessageStateUseCase: UpdateMessageStateUseCase,
    private val getMessageFromLocalUseCase: GetMessageFromLocalUseCase
) {
    suspend operator fun invoke(messageData: MessageData, rid: Long, networkState: Boolean) {
        withContext(Dispatchers.IO) {
            if (networkState) {
                val messageId =
                    getMessageFromLocalUseCase(messageData).firstOrNull()?.id
                if (messageId != null) {
                    val request = try {
                        sendMessageToRemoteUseCase(messageData).first()
                    } catch (e: Throwable) {
                        println(e)
                    }
                    if (request is MessageSendState) {
                        val message = messageData.copy(
                            messageSendState = request,
                            time = Instant.now().toEpochMilli()
                        )
                        updateMessageStateUseCase(messageId, rid, message)
                    }
                }
            }
        }
    }
}
