package com.gyleedev.domain.usecase

import com.gyleedev.domain.model.GetChatRoomState
import com.gyleedev.domain.repository.ChatRoomRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetChatRoomByRidUseCase @Inject constructor(
    private val repository: ChatRoomRepository,
) {
    suspend operator fun invoke(
        rid: String,
        getChatRoomState: GetChatRoomState = GetChatRoomState.CheckAndGetDataFromLocal,
    ): GetChatRoomState = withContext(Dispatchers.IO) {
        repository.getChatRoomWithRid(rid, getChatRoomState)
    }
}
