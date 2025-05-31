package com.gyleedev.chatchat.domain.usecase

import com.gyleedev.chatchat.data.repository.ChatRoomRepository
import com.gyleedev.chatchat.domain.ChatRoomData
import com.gyleedev.chatchat.domain.RelatedUserLocalData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CreateFriendChatRoomUseCase @Inject constructor(
    private val repository: ChatRoomRepository
) {
    suspend operator fun invoke(
        friend: RelatedUserLocalData,
        chatRoomData: ChatRoomData
    ) {
        return withContext(Dispatchers.IO) {
            repository.createFriendUserChatRoom(friend, chatRoomData)
        }
    }
}
