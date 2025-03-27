package com.gyleedev.chatchat.domain.usecase

import com.gyleedev.chatchat.domain.MessageData
import com.gyleedev.chatchat.domain.MessageSendState
import com.gyleedev.chatchat.domain.MessageType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SendMessageUseCase @Inject constructor(
    private val insertMessageToLocalUseCase: InsertMessageToLocalUseCase,
    private val sendMessageToRemoteUseCase: SendMessageToRemoteUseCase,
    private val updateMessageStateUseCase: UpdateMessageStateUseCase,
    private val uploadImageToRemoteUseCase: UploadImageToRemoteUseCase
) {
    suspend operator fun invoke(messageData: MessageData, rid: Long, networkState: Boolean) {
        withContext(Dispatchers.IO) {
            val message = if (messageData.type == MessageType.Photo) {
                messageData.copy(
                    comment = uploadImageToRemoteUseCase(messageData.comment).first()
                )
            } else {
                messageData
            }
            val messageId = insertMessageToLocalUseCase(message, rid)
            if (networkState) {
                val request = try {
                    sendMessageToRemoteUseCase(message).first()
                } catch (e: Throwable) {
                    println(e)
                }
                if (request is MessageSendState) {
                    val updateMessage = message.copy(messageSendState = request)
                    updateMessageStateUseCase(messageId, rid, updateMessage)
                }
            } else {
                val updateMessage = message.copy(messageSendState = MessageSendState.FAIL)
                updateMessageStateUseCase(messageId, rid, updateMessage)
            }
        }
    }
}
