package com.gyleedev.domain.usecase

import com.gyleedev.domain.model.UserData
import com.gyleedev.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SignInAuthUseCase @Inject constructor(
    private val repository: UserRepository,
) {
    suspend operator fun invoke(id: String, password: String, nickname: String): Flow<UserData?> =
        withContext(Dispatchers.IO) {
            repository.signInUser(id, password, nickname)
        }
}
