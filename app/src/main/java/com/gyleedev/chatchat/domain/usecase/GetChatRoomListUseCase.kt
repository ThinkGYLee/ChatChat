package com.gyleedev.chatchat.domain.usecase

import androidx.paging.PagingData
import com.gyleedev.chatchat.data.repository.UserRepository
import com.gyleedev.chatchat.domain.ChatRoomDataWithRelatedUsers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetChatRoomListUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(): Flow<PagingData<ChatRoomDataWithRelatedUsers>> {
        return withContext(Dispatchers.IO) {
            repository.getChatRoomListFromLocal()
        }
    }
}
