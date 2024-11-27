package com.gyleedev.chatchat.domain.usecase

import com.gyleedev.chatchat.domain.ChatRoomData
import com.gyleedev.chatchat.domain.FriendData
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
    suspend operator fun invoke(friendData: FriendData): ChatRoomData? {
        return withContext(Dispatchers.IO) {
            val data: ChatRoomData?
            runBlocking {
                data = createChatRoomUseCase().firstOrNull()
            }
            if (data != null) {
                insertChatRoomToLocalUseCase(friendData, data)
                createMyChatRoomUseCase(friendData, data)
                createFriendChatRoomUseCase(friendData, data)
            }
            data
        }
    }
}
