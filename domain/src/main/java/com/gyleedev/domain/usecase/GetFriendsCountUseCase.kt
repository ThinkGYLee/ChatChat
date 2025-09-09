package com.gyleedev.domain.usecase

import com.gyleedev.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetFriendsCountUseCase @Inject constructor(
    private val repository: UserRepository,
) {
    suspend operator fun invoke(): Long = withContext(Dispatchers.IO) {
        repository.getFriendsCount()
    }
}
