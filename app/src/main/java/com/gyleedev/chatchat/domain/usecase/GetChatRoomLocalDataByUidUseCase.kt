package com.gyleedev.chatchat.domain.usecase

import com.gyleedev.chatchat.data.repository.UserRepository
import com.gyleedev.chatchat.domain.ChatRoomLocalData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetChatRoomLocalDataByUidUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(uid: String): ChatRoomLocalData {
        return withContext(Dispatchers.IO) {
            repository.getChatRoomByUid(uid)
        }
    }
}
