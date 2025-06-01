package com.gyleedev.chatchat.domain.usecase

import com.gyleedev.chatchat.domain.model.ChatRoomLocalDataWrapper
import com.gyleedev.chatchat.domain.model.RelatedUserLocalData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

// 최상위 유스케이스
class GetChatRoomDataUseCase @Inject constructor(
    private val getChatRoomDataFromLocalUseCase: GetChatRoomDataFromLocalUseCase,
    private val getChatRoomRemoteDataUseCase: GetChatRoomRemoteDataUseCase
) {
    suspend operator fun invoke(relatedUserLocalData: RelatedUserLocalData) {
        return withContext(Dispatchers.IO) {
            val localData = getChatRoomDataFromLocalUseCase(relatedUserLocalData)
            if (localData is ChatRoomLocalDataWrapper.Failure) {
                getChatRoomRemoteDataUseCase(relatedUserLocalData)
            }
        }
    }
}
