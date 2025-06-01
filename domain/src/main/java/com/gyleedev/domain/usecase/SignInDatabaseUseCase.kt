package com.gyleedev.domain.usecase

import com.gyleedev.chatchat.domain.model.SignInResult
import com.gyleedev.chatchat.domain.model.UserData
import com.gyleedev.chatchat.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SignInDatabaseUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(userData: UserData): Flow<SignInResult> {
        return withContext(Dispatchers.IO) {
            repository.writeUserToRemote(userData)
        }
    }
}
