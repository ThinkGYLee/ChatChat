package com.gyleedev.domain.usecase

import com.gyleedev.domain.model.ChatRoomData
import com.gyleedev.domain.model.RelatedUserLocalData
import com.gyleedev.domain.repository.ChatRoomRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class InsertChatRoomToLocalUseCase @Inject constructor(
    private val repository: ChatRoomRepository
) {
    suspend operator fun invoke(
        relatedUserLocalData: RelatedUserLocalData,
        chatRoomData: ChatRoomData
    ): Long {
        return withContext(Dispatchers.IO) {
            repository.insertChatRoomToLocal(relatedUserLocalData, chatRoomData)
        }
    }
}
