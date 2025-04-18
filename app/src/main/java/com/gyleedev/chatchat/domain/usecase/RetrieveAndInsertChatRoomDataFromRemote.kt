package com.gyleedev.chatchat.domain.usecase

import com.gyleedev.chatchat.data.repository.UserRepository
import com.gyleedev.chatchat.domain.ChatRoomData
import com.gyleedev.chatchat.domain.RelatedUserLocalData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RetrieveAndInsertChatRoomDataFromRemote @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(relatedUserLocalData: RelatedUserLocalData): ChatRoomData? {
        return withContext(Dispatchers.IO) {
            var result: ChatRoomData?
            runBlocking {
                result = repository.getChatRoomFromRemote(relatedUserLocalData).firstOrNull()
            }
            if (result != null) {
                repository.insertChatRoomToLocal(relatedUserLocalData, result!!)
            }
            result
        }
    }
}
