package com.gyleedev.domain.usecase

import com.gyleedev.domain.model.UserData
import com.gyleedev.domain.repository.UserRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class AddFriendToRemoteUseCase @Inject constructor(
    private val repository: UserRepository,
) {
    suspend operator fun invoke(userData: UserData): Boolean = repository.addRelatedUserToRemote(userData).first()
}
