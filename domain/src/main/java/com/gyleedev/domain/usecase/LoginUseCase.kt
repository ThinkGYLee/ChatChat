package com.gyleedev.domain.usecase

import com.gyleedev.domain.model.LogInResult
import com.gyleedev.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(id: String, password: String): Flow<LogInResult> {
        return withContext(Dispatchers.IO) {
            repository.loginRequest(id, password)
        }
    }
}
