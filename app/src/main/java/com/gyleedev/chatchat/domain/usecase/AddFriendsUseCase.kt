package com.gyleedev.chatchat.domain.usecase

import com.gyleedev.chatchat.data.repository.UserRepository
import com.gyleedev.chatchat.domain.UserData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AddFriendsUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(friends: List<UserData>) {
        return withContext(Dispatchers.IO) {
            repository.insertMyFriendListToLocal(friends)
        }
    }
}
