package com.gyleedev.chatchat.domain.usecase

import com.gyleedev.chatchat.domain.ChatRoomLocalDataWrapper
import com.gyleedev.chatchat.domain.FriendData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

// 채팅방 정보를 로컬에서 가져오는 함수
class GetChatRoomDataFromLocalUseCase @Inject constructor(
    private val getChatRoomLocalDataByUidUseCase: GetChatRoomLocalDataByUidUseCase
) {
    suspend operator fun invoke(friendData: FriendData): ChatRoomLocalDataWrapper {
        return withContext(Dispatchers.IO) {
            try {
                ChatRoomLocalDataWrapper.Success(getChatRoomLocalDataByUidUseCase(friendData.uid))
            } catch (e: Exception) {
                ChatRoomLocalDataWrapper.Failure
            }
        }
    }
}
