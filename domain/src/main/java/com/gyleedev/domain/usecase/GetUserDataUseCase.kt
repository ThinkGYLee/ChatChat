package com.gyleedev.domain.usecase

import com.gyleedev.domain.model.SearchUserResult
import com.gyleedev.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetUserDataUseCase @Inject constructor(
    private val repository: UserRepository,
) {
    suspend operator fun invoke(email: String): Flow<SearchUserResult> = withContext(Dispatchers.IO) {
        repository.searchUserRequest(email)
    }
}
