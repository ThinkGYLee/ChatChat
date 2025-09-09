package com.gyleedev.domain.usecase

import com.gyleedev.domain.model.SignInResult
import com.gyleedev.domain.model.UserData
import com.gyleedev.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SignInDatabaseUseCase @Inject constructor(
    private val repository: UserRepository,
) {
    suspend operator fun invoke(userData: UserData): Flow<SignInResult> = withContext(Dispatchers.IO) {
        repository.writeUserToRemote(userData)
    }
}
