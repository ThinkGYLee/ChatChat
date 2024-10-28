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
            repository.insertMessageToLocal(messageData, rid)
        }
    }
}
