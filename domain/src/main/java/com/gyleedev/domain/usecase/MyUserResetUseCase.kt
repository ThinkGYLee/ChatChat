package com.gyleedev.domain.usecase

import com.gyleedev.domain.repository.UserRepository
import javax.inject.Inject

class MyUserResetUseCase @Inject constructor(
    private val repository: UserRepository,
) {
    suspend operator fun invoke() {
        repository.resetMyUserData()
    }
}
