package com.gyleedev.domain.usecase

import androidx.paging.PagingData
import androidx.paging.map
import com.gyleedev.domain.model.ChatRoomDataWithAllRelatedUsersAndMessage
import com.gyleedev.domain.model.RelatedUserLocalData
import com.gyleedev.domain.repository.ChatRoomRepository
import com.gyleedev.domain.repository.MessageRepository
import com.gyleedev.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetChatRoomDataWithRelatedUserUseCase @Inject constructor(
    private val chatRoomRepository: ChatRoomRepository,
    private val userRepository: UserRepository,
    private val messageRepository: MessageRepository
) {
    operator fun invoke(): Flow<PagingData<ChatRoomDataWithAllRelatedUsersAndMessage>> {
        return chatRoomRepository.getChatRoomListWithPaging().map { pagingData ->
            pagingData.map { chatRoom ->
                ChatRoomDataWithAllRelatedUsersAndMessage(
                    chatRoomAndReceiverLocalData = chatRoom,
                    receiversInfo = chatRoom.receivers.map {
                        val localUser = userRepository.getFriendAndFavoriteByUid(it)
                        if (localUser != null) {
                            localUser
                        } else {
                            RelatedUserLocalData()
                        }
                    },
                    lastMessageData = messageRepository.getLastMessage(chatRoom.rid)
                )
            }
        }.flowOn(Dispatchers.IO)
    }
}
