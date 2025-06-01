package com.gyleedev.chatchat.domain.usecase

import com.gyleedev.chatchat.domain.repository.ChatRoomRepository
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
