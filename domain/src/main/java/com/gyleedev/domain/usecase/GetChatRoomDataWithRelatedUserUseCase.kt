package com.gyleedev.domain.usecase

import androidx.paging.PagingData
import androidx.paging.map
import com.gyleedev.domain.model.ChatRoomDataWithRelatedUsers
import com.gyleedev.domain.repository.ChatRoomRepository
import com.gyleedev.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetChatRoomDataWithRelatedUserUseCase @Inject constructor(
    private val chatRoomRepository: ChatRoomRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): Flow<PagingData<ChatRoomDataWithRelatedUsers>> {
        return withContext(Dispatchers.IO) {
            val relatedUsers = userRepository.getRelatedUsersForChatRoomList()
            chatRoomRepository.getChatRoomListWithPaging().map { pagingData ->
                pagingData.map { chatRoom ->
                    ChatRoomDataWithRelatedUsers(
                        chatRoomLocalData = chatRoom,
                        relatedUserLocalData = relatedUsers.find { chatRoom.receiver == it.uid }!!
                    )
                }
            }
        }
    }
}
