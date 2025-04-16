package com.gyleedev.chatchat.domain.usecase

import com.gyleedev.chatchat.data.repository.UserRepository
import com.gyleedev.chatchat.domain.RelatedUserLocalData
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class CheckChatRoomExistsUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(friend: RelatedUserLocalData): Boolean {
        return repository.checkChatRoomExistsInRemote(friend).firstOrNull() ?: false
    }
}
