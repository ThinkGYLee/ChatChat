package com.gyleedev.domain.usecase

import com.gyleedev.domain.repository.ChatRoomRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CreateLocalChatRoomUseCase @Inject constructor(
    private val repository: ChatRoomRepository
) {
    suspend operator fun invoke(rid: String, receiver: String): Long {
        return withContext(Dispatchers.IO) {
            repository.makeNewChatRoom(rid, receiver)
        }
    }
}
