package com.gyleedev.chatchat.domain.usecase

import com.gyleedev.chatchat.data.repository.UserRepository
import com.gyleedev.chatchat.domain.ChatRoomData
import com.gyleedev.chatchat.domain.FriendData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RetrieveAndInsertChatRoomDataFromRemote @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(friendData: FriendData): ChatRoomData? {
        return withContext(Dispatchers.IO) {
            var result: ChatRoomData?
            runBlocking {
                result = repository.getChatRoomFromRemote(friendData).firstOrNull()
            }
            if (result != null) {
                repository.insertChatRoomToLocal(friendData, result!!)
            }
            result
        }
    }
}
