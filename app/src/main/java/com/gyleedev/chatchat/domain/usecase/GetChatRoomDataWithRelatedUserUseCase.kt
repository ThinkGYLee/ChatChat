package com.gyleedev.chatchat.domain.usecase

import androidx.paging.PagingData
import androidx.paging.map
import com.gyleedev.chatchat.domain.model.ChatRoomDataWithRelatedUsers
import com.gyleedev.chatchat.domain.repository.ChatRoomRepository
import com.gyleedev.chatchat.domain.repository.UserRepository
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
            chatRoomRepository.getChatRoomListWithPaging().map { value ->
                value.map { chatroom ->
                    ChatRoomDataWithRelatedUsers(
                        chatRoomLocalData = chatroom,
                        relatedUserLocalData = relatedUsers.find { chatroom.receiver == it.uid }!!
                    )
                }
            }
        }
    }
}
