package com.gyleedev.domain.usecase

import com.gyleedev.domain.model.GetChatRoomState
import com.gyleedev.domain.model.RelatedUserLocalData
import com.gyleedev.domain.repository.ChatRoomRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetChatRoomUseCase @Inject constructor(
    private val repository: ChatRoomRepository
) {
    suspend operator fun invoke(
        user: RelatedUserLocalData,
        getChatRoomState: GetChatRoomState = GetChatRoomState.CheckAndGetDataFromLocal
    ): GetChatRoomState {
        return withContext(Dispatchers.IO) {
            repository.getChatRoom(user, getChatRoomState)
        }
    }
}
