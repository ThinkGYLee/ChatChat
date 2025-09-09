package com.gyleedev.domain.usecase

import com.gyleedev.domain.repository.MessageRepository
import javax.inject.Inject

class GetMessagesFromLocalUseCase @Inject constructor(
    private val repository: MessageRepository,
) {
    operator fun invoke(rid: String) = repository.getMessagesFromLocal(rid)
}
