package com.gyleedev.chatchat.domain.usecase

import com.gyleedev.chatchat.data.repository.UserRepository
import com.gyleedev.chatchat.domain.MessageData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SendMessageUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(messageData: MessageData, rid: Long) {
        return withContext(Dispatchers.IO) {
            val messageId = repository.insertMessageToLocal(messageData, rid)
            if (messageId != null) {
                val request = repository.insertMessageToRemote(messageData)
                request.collect { it ->
                    it.also {
                        val message = messageData.copy(messageSendState = it)

                        repository.updateMessageState(messageId, rid, message)
                    }
                }
            }
        }
    }
}
