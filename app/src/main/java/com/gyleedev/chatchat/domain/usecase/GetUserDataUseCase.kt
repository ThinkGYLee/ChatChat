package com.gyleedev.chatchat.domain.usecase

import com.gyleedev.chatchat.domain.model.SearchUserResult
import com.gyleedev.chatchat.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetUserDataUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(email: String): Flow<SearchUserResult> {
        return withContext(Dispatchers.IO) {
            repository.searchUserRequest(email)
        }
    }
}
