package com.gyleedev.chatchat.domain.usecase

import com.gyleedev.chatchat.domain.model.UserData
import com.gyleedev.chatchat.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AddFriendToRemoteUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(userData: UserData): Flow<Boolean> =
        repository.addRelatedUserToRemote(userData)
}
