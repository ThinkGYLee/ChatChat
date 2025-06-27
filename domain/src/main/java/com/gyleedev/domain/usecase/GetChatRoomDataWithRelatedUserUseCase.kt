package com.gyleedev.domain.usecase

import androidx.paging.PagingData
import androidx.paging.map
import com.gyleedev.domain.model.ChatRoomDataWithAllRelatedUsersAndMessage
import com.gyleedev.domain.model.MessageData
import com.gyleedev.domain.repository.ChatRoomRepository
import com.gyleedev.domain.repository.MessageRepository
import com.gyleedev.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
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
                chatRoom.receivers.map {
                    userRepository.getFriendAndFavoriteByUid(it)
                }
                ChatRoomDataWithAllRelatedUsersAndMessage(
                    chatRoomAndReceiverLocalData = chatRoom,
                    receiversInfo = chatRoom.receivers.map {
                        userRepository.getFriendAndFavoriteByUid(it).first()
                    },
                    lastMessageData = messageRepository.getLastMessage(chatRoom.rid)
                        ?: MessageData()
                )
            }
        }.flowOn(Dispatchers.IO)
    }
}
