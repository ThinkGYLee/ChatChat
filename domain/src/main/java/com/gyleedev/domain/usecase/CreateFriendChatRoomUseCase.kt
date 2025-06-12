package com.gyleedev.domain.usecase

import com.gyleedev.domain.model.ChatRoomData
import com.gyleedev.domain.model.RelatedUserLocalData
import com.gyleedev.domain.repository.ChatRoomRepository
import javax.inject.Inject

class CreateFriendChatRoomUseCase @Inject constructor(
    private val repository: ChatRoomRepository
) {
    operator fun invoke(
        friend: RelatedUserLocalData,
        chatRoomData: ChatRoomData
    ) = repository.createFriendUserChatRoom(friend, chatRoomData)
}
