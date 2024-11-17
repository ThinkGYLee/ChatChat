package com.gyleedev.chatchat.domain.usecase

import com.gyleedev.chatchat.domain.ChatRoomLocalDataWrapper
import com.gyleedev.chatchat.domain.FriendData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

// 최상위 유스케이스
class GetChatRoomDataUseCase @Inject constructor(
    private val getChatRoomDataFromLocalUseCase: GetChatRoomDataFromLocalUseCase,
    private val getChatRoomRemoteDataUseCase: GetChatRoomRemoteDataUseCase
) {
    suspend operator fun invoke(friendData: FriendData) {
        return withContext(Dispatchers.IO) {
            val localData = getChatRoomDataFromLocalUseCase(friendData)
            if (localData is ChatRoomLocalDataWrapper.Failure) {
                getChatRoomRemoteDataUseCase(friendData)
            }
        }
    }
}
