package com.gyleedev.chatchat.domain.usecase

import com.gyleedev.chatchat.data.repository.UserRepository
import com.gyleedev.chatchat.domain.SignInResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SignInUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(id: String, password: String): SignInResult {
        return withContext(Dispatchers.IO) {
            val authAsync = async { repository.signInUser(id, password) }
            val authResult = authAsync.await()
            val databaseAsync = async { authResult?.let { repository.writeUserToRealtimeDatabase(it) } }
            if (databaseAsync.await() == SignInResult.Success) {
                SignInResult.Success
            } else {
                SignInResult.Failure
            }
        }
    }
}
