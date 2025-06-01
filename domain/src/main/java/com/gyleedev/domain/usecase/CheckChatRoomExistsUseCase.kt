package com.gyleedev.domain.usecase

import com.gyleedev.domain.model.RelatedUserLocalData
import com.gyleedev.domain.repository.ChatRoomRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class CheckChatRoomExistsUseCase @Inject constructor(
    private val repository: ChatRoomRepository
) {
    suspend operator fun invoke(friend: RelatedUserLocalData): Boolean {
        return repository.checkChatRoomExistsInRemote(friend).firstOrNull() ?: false
    }
}
