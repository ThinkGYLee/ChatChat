package com.gyleedev.chatchat.domain.usecase

import com.gyleedev.chatchat.domain.FriendData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

//
class InsertChatRoomToLocalWithRemoteAccessUseCase @Inject constructor(
    private val getChatRoomFromRemoteByFriendUseCase: GetChatRoomFromRemoteByFriendUseCase,
    private val insertChatRoomToLocalUseCase: InsertChatRoomToLocalUseCase,
    private val createChatRoomsUseCase: CreateChatRoomsUseCase
) {
    suspend operator fun invoke(friendData: FriendData) {
        return withContext(Dispatchers.IO) {
            val request = getChatRoomFromRemoteByFriendUseCase(friendData)
            request.collect {
                if (it != null) {
                    insertChatRoomToLocalUseCase(friendData, it)
                } else {
                    createChatRoomsUseCase(friendData)
                }
            }
        }
    }
}
