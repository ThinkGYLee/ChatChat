package com.gyleedev.chatchat.domain.usecase

import com.gyleedev.chatchat.data.repository.UserRepository
import com.gyleedev.chatchat.domain.ChatRoomData
import com.gyleedev.chatchat.domain.FriendData
import com.gyleedev.chatchat.domain.UserChatRoomData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CreateFriendChatRoomUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(
        friend: FriendData,
        chatRoomData: ChatRoomData
    ): Flow<UserChatRoomData?> {
        return withContext(Dispatchers.IO) {
            repository.createFriendUserChatRoom(friend, chatRoomData)
        }
    }
}
