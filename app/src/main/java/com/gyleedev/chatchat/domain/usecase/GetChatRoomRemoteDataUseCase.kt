package com.gyleedev.chatchat.domain.usecase

import com.gyleedev.chatchat.domain.model.ChatRoomData
import com.gyleedev.chatchat.domain.model.RelatedUserLocalData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetChatRoomRemoteDataUseCase @Inject constructor(
    private val checkChatRoomExistsUseCase: CheckChatRoomExistsUseCase,
    private val createChatRoomsUseCase: CreateChatRoomsUseCase,
    private val retrieveAndInsertChatRoomDataFromRemoteUseCase: RetrieveAndInsertChatRoomDataFromRemoteUseCase
) {
    suspend operator fun invoke(relatedUserLocalData: RelatedUserLocalData): ChatRoomData? {
        return withContext(Dispatchers.IO) {
            // 존재하나 확인하기
            val checkRemote = checkChatRoomExistsUseCase(relatedUserLocalData)
            if (checkRemote) {
                retrieveAndInsertChatRoomDataFromRemoteUseCase(relatedUserLocalData)
            } else {
                createChatRoomsUseCase(relatedUserLocalData)
            }
        }
    }
}
