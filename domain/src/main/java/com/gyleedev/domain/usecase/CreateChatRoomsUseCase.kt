package com.gyleedev.domain.usecase

import com.gyleedev.chatchat.domain.model.ChatRoomData
import com.gyleedev.chatchat.domain.model.RelatedUserLocalData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CreateChatRoomsUseCase @Inject constructor(
    private val createChatRoomUseCase: CreateChatRoomUseCase,
    private val createMyChatRoomUseCase: CreateMyChatRoomUseCase,
    private val createFriendChatRoomUseCase: CreateFriendChatRoomUseCase,
    private val insertChatRoomToLocalUseCase: InsertChatRoomToLocalUseCase
) {
    suspend operator fun invoke(relatedUserLocalData: RelatedUserLocalData): ChatRoomData? {
        return withContext(Dispatchers.IO) {
            val data: ChatRoomData?
            runBlocking {
                data = createChatRoomUseCase().firstOrNull()
            }
            if (data != null) {
                insertChatRoomToLocalUseCase(relatedUserLocalData, data)
                createMyChatRoomUseCase(relatedUserLocalData, data)
                createFriendChatRoomUseCase(relatedUserLocalData, data)
            }
            data
        }
    }
}
