package com.gyleedev.chatchat.domain.usecase

import com.gyleedev.chatchat.data.repository.UserRepository
import com.gyleedev.chatchat.domain.ChatRoomData
import com.gyleedev.chatchat.domain.DeleteFriendState
import com.gyleedev.chatchat.domain.FriendData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DeleteFriendUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(friend: FriendData) = repository.deleteFriendRequest(friend)
}
