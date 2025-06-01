package com.gyleedev.domain.usecase

import com.gyleedev.chatchat.domain.model.ChatRoomLocalData
import com.gyleedev.chatchat.domain.repository.ChatRoomRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetChatRoomLocalDataByUidUseCase @Inject constructor(
    private val repository: ChatRoomRepository
) {
    suspend operator fun invoke(uid: String): ChatRoomLocalData {
        return withContext(Dispatchers.IO) {
            repository.getChatRoomByUid(uid)
        }
    }
}
