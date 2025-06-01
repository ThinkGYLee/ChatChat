package com.gyleedev.domain.usecase

import com.gyleedev.domain.model.ChatRoomLocalDataWrapper
import com.gyleedev.domain.model.RelatedUserLocalData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

// 채팅방 정보를 로컬에서 가져오는 함수
class GetChatRoomDataFromLocalUseCase @Inject constructor(
    private val getChatRoomLocalDataByUidUseCase: GetChatRoomLocalDataByUidUseCase
) {
    suspend operator fun invoke(relatedUserLocalData: RelatedUserLocalData): ChatRoomLocalDataWrapper {
        return withContext(Dispatchers.IO) {
            try {
                ChatRoomLocalDataWrapper.Success(getChatRoomLocalDataByUidUseCase(relatedUserLocalData.uid))
            } catch (e: Exception) {
                ChatRoomLocalDataWrapper.Failure
            }
        }
    }
}
