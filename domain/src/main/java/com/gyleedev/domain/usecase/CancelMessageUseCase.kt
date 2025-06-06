package com.gyleedev.domain.usecase

import com.gyleedev.domain.model.MessageData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CancelMessageUseCase @Inject constructor(
    private val deleteMessageFromLocalUseCase: DeleteMessageFromLocalUseCase,
    private val getMessageFromLocalUseCase: GetMessageFromLocalUseCase
) {
    suspend operator fun invoke(messageData: MessageData) {
        withContext(Dispatchers.IO) {
            val messageId =
                getMessageFromLocalUseCase(messageData).firstOrNull()?.messageId
            if (messageId != null) {
                deleteMessageFromLocalUseCase(messageId)
            }
        }
    }
}
