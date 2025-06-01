package com.gyleedev.chatchat.domain.usecase

import com.gyleedev.chatchat.domain.model.ChatRoomData
import com.gyleedev.chatchat.domain.model.RelatedUserLocalData
import com.gyleedev.chatchat.domain.repository.ChatRoomRepository
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
