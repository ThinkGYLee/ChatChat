package com.gyleedev.domain.usecase

import com.gyleedev.domain.repository.UserRepository
import javax.inject.Inject

class GetVerifiedStateUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke() =
        repository.getVerifiedState()
}
