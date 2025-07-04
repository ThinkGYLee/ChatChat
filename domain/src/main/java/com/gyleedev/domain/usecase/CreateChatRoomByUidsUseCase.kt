package com.gyleedev.domain.usecase

import com.gyleedev.domain.model.GetChatRoomState
import com.gyleedev.domain.model.RelatedUserLocalData
import com.gyleedev.domain.repository.ChatRoomRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CreateChatRoomByUidsUseCase @Inject constructor(
    private val chatRoomRepository: ChatRoomRepository
) {
    suspend operator fun invoke(userList: List<RelatedUserLocalData>): GetChatRoomState {
        return withContext(Dispatchers.IO) {
            if (userList.size > 1) {
                chatRoomRepository.createGroupChat(userList)
            } else {
                chatRoomRepository.getChatRoomWithUserData(
                    userList[0],
                    getChatRoomState = GetChatRoomState.CheckAndGetDataFromLocal
                )
            }
        }
    }
}
