package com.gyleedev.domain.usecase

import com.gyleedev.chatchat.domain.model.ChatRoomData
import com.gyleedev.chatchat.domain.model.RelatedUserLocalData
import com.gyleedev.chatchat.domain.repository.ChatRoomRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RetrieveAndInsertChatRoomDataFromRemoteUseCase @Inject constructor(
    private val repository: ChatRoomRepository
) {
    suspend operator fun invoke(relatedUserLocalData: RelatedUserLocalData): ChatRoomData? {
        return withContext(Dispatchers.IO) {
            var result: ChatRoomData?
            runBlocking {
                result = repository.getChatRoomFromRemote(relatedUserLocalData).firstOrNull()
            }
            if (result != null) {
                repository.insertChatRoomToLocal(relatedUserLocalData, result)
            }
            result
        }
    }
}
