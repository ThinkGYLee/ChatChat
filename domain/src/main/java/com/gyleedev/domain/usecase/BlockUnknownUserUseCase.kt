package com.gyleedev.domain.usecase

import com.gyleedev.domain.model.ChangeRelationResult
import com.gyleedev.domain.model.UserData
import com.gyleedev.domain.repository.UserRepository
import javax.inject.Inject

class BlockUnknownUserUseCase @Inject constructor(
    private val repository: UserRepository,
) {
    suspend operator fun invoke(userData: UserData): ChangeRelationResult {
        return repository.blockUnknownUserRequest(userData)
    }
}
