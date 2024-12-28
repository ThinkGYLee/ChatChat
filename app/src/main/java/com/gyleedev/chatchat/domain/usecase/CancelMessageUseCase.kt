package com.gyleedev.chatchat.domain.usecase

import com.gyleedev.chatchat.domain.MessageData
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
                getMessageFromLocalUseCase(messageData).firstOrNull()?.id
            if (messageId != null) {
                deleteMessageFromLocalUseCase(messageId)
            }
        }
    }
}
