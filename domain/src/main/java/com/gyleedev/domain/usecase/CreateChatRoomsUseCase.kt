package com.gyleedev.domain.usecase

import com.gyleedev.domain.model.GetChatRoomResult
import com.gyleedev.domain.model.ProcessResult
import com.gyleedev.domain.model.RelatedUserLocalData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CreateChatRoomsUseCase @Inject constructor(
    private val createChatRoomUseCase: CreateChatRoomUseCase,
    private val createMyChatRoomUseCase: CreateMyChatRoomUseCase,
    private val createFriendChatRoomUseCase: CreateFriendChatRoomUseCase,
    private val insertChatRoomToLocalUseCase: InsertChatRoomToLocalUseCase
) {
    suspend operator fun invoke(relatedUserLocalData: RelatedUserLocalData): GetChatRoomResult {
        return withContext(Dispatchers.IO) {
            val data = createChatRoomUseCase().firstOrNull()
            return@withContext if (data != null) {
                insertChatRoomToLocalUseCase(relatedUserLocalData, data)
                val remoteMyResult = createMyChatRoomUseCase(relatedUserLocalData, data).first()
                val remoteFriendResult =
                    createFriendChatRoomUseCase(relatedUserLocalData, data).first()
                if (remoteFriendResult == ProcessResult.Success && remoteMyResult == ProcessResult.Success) {
                    GetChatRoomResult.Success(
                        chatRoom = data
                    )
                } else {
                    GetChatRoomResult.Failure
                }
            } else {
                GetChatRoomResult.Failure
            }
        }
    }
}
