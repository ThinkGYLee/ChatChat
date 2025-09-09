package com.gyleedev.domain.usecase

import com.gyleedev.domain.model.VerifiedState
import com.gyleedev.domain.repository.UserRepository
import javax.inject.Inject

class SetVerifiedStateUseCase @Inject constructor(
    private val repository: UserRepository,
) {
    suspend operator fun invoke(verifiedState: VerifiedState) = repository.setVerifiedState(verifiedState)
}
