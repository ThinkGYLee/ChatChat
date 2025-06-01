package com.gyleedev.chatchat.domain.usecase

import com.gyleedev.chatchat.domain.repository.MessageRepository
import javax.inject.Inject

class GetMessagesFromLocalUseCase @Inject constructor(
    private val repository: MessageRepository
) {
    operator fun invoke(rid: String) = repository.getMessagesFromLocal(rid)
}
