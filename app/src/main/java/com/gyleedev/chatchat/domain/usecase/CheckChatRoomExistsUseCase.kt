package com.gyleedev.chatchat.domain.usecase

import com.gyleedev.chatchat.data.repository.UserRepository
import com.gyleedev.chatchat.domain.FriendData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CheckChatRoomExistsUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(friend: FriendData): Boolean {
        return withContext(Dispatchers.IO) {
            var result: Boolean
            runBlocking {
                result = repository.checkChatRoomExistsInRemote(friend).firstOrNull() ?: false
            }
            result
        }
    }
}
