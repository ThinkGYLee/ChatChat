package com.gyleedev.domain.usecase

import com.gyleedev.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class GetMyDataFromRemoteUseCase @Inject constructor(
    private val repository: UserRepository,
) {
    suspend operator fun invoke() = repository.getMyDataFromRemote().flowOn(Dispatchers.IO)
}
