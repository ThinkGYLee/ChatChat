package com.gyleedev.domain.usecase

import com.gyleedev.domain.model.GetChatRoomResult
import com.gyleedev.domain.model.RelatedUserLocalData
import com.gyleedev.domain.repository.ChatRoomRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RetrieveAndInsertChatRoomDataFromRemoteUseCase @Inject constructor(
    private val repository: ChatRoomRepository
) {
    suspend operator fun invoke(relatedUserLocalData: RelatedUserLocalData): GetChatRoomResult {
        return withContext(Dispatchers.IO) {
            var result = repository.getChatRoomFromRemote(relatedUserLocalData).firstOrNull()
            if (result != null) {
                repository.insertChatRoomToLocal(relatedUserLocalData, result)
                GetChatRoomResult.Success(
                    chatRoom = result
                )
            } else {
                GetChatRoomResult.Failure
            }
        }
    }
}
