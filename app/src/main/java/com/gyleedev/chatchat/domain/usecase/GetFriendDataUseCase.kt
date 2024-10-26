package com.gyleedev.chatchat.domain.usecase

import com.gyleedev.chatchat.data.repository.UserRepository
import com.gyleedev.chatchat.domain.FriendData
import com.gyleedev.chatchat.domain.UserData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetFriendDataUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(friend: String): FriendData {
        return withContext(Dispatchers.IO) {
            repository.getFriendById(friend)
        }
    }
}
