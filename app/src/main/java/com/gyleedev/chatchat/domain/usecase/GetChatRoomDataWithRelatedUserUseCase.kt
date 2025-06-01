package com.gyleedev.chatchat.domain.usecase

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.gyleedev.chatchat.data.database.entity.toModel
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
            Pager(
                config = PagingConfig(pageSize = 10, enablePlaceholders = false),
                pagingSourceFactory = {
                    chatRoomRepository.getChatRoomListWithPaging()
                }
            ).flow.map { value ->
                value.map { chatRoom ->
                    ChatRoomDataWithRelatedUsers(
                        chatRoomLocalData = chatRoom.toModel(),
                        relatedUserLocalData = relatedUsers.find { chatRoom.receiver == it.uid }!!
                    )
                }
            }
        }
    }
}
