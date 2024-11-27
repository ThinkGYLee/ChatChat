package com.gyleedev.chatchat.domain.usecase

import com.gyleedev.chatchat.domain.ChatRoomData
import com.gyleedev.chatchat.domain.FriendData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetChatRoomRemoteDataUseCase @Inject constructor(
    private val checkChatRoomExistsUseCase: CheckChatRoomExistsUseCase,
    private val createChatRoomsUseCase: CreateChatRoomsUseCase,
    private val retrieveAndInsertChatRoomDataFromRemote: RetrieveAndInsertChatRoomDataFromRemote
) {
    suspend operator fun invoke(friendData: FriendData): ChatRoomData? {
        return withContext(Dispatchers.IO) {
            // 존재하나 확인하기
            val checkRemote = checkChatRoomExistsUseCase(friendData)
            if (checkRemote) {
                retrieveAndInsertChatRoomDataFromRemote(friendData)
            } else {
                createChatRoomsUseCase(friendData)
            }
        }
    }
}
