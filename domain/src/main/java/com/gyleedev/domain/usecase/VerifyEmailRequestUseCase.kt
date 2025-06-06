package com.gyleedev.domain.usecase

import com.gyleedev.domain.repository.UserRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class VerifyEmailRequestUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke() = repository.verifyEmailRequest().first()
}
