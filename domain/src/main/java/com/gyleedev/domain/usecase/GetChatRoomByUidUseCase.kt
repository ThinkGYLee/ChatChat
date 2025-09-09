package com.gyleedev.domain.usecase

import com.gyleedev.domain.model.GetChatRoomState
import com.gyleedev.domain.model.RelatedUserLocalData
import com.gyleedev.domain.repository.ChatRoomRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetChatRoomByUidUseCase @Inject constructor(
    private val repository: ChatRoomRepository,
) {
    suspend operator fun invoke(
        user: RelatedUserLocalData,
        getChatRoomState: GetChatRoomState = GetChatRoomState.CheckAndGetDataFromLocal,
    ): GetChatRoomState = withContext(Dispatchers.IO) {
        repository.getChatRoomWithUserData(user, getChatRoomState)
    }
}
