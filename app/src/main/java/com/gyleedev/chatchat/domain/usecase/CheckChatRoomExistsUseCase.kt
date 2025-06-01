package com.gyleedev.chatchat.domain.usecase

import com.gyleedev.chatchat.domain.model.RelatedUserLocalData
import com.gyleedev.chatchat.domain.repository.ChatRoomRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class CheckChatRoomExistsUseCase @Inject constructor(
    private val repository: ChatRoomRepository
) {
    suspend operator fun invoke(friend: RelatedUserLocalData): Boolean {
        return repository.checkChatRoomExistsInRemote(friend).firstOrNull() ?: false
    }
}
