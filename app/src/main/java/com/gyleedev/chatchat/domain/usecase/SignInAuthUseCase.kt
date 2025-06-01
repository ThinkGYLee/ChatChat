package com.gyleedev.chatchat.domain.usecase

import com.gyleedev.chatchat.domain.model.UserData
import com.gyleedev.chatchat.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SignInAuthUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(id: String, password: String, nickname: String): Flow<UserData?> {
        return withContext(Dispatchers.IO) {
            repository.signInUser(id, password, nickname)
        }
    }
}
