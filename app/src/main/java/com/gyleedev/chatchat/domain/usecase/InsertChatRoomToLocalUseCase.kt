package com.gyleedev.chatchat.domain.usecase

import com.gyleedev.chatchat.data.repository.UserRepository
import com.gyleedev.chatchat.domain.ChatRoomData
import com.gyleedev.chatchat.domain.FriendData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class InsertChatRoomToLocalUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(friendData: FriendData, chatRoomData: ChatRoomData): Long {
        return withContext(Dispatchers.IO) {
            repository.insertChatRoomToLocal(friendData, chatRoomData)
        }
    }
}
