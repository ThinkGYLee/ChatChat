package com.gyleedev.domain.usecase

import com.gyleedev.domain.model.RelatedUserRemoteData
import com.gyleedev.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetMyRelatedUserListFromRemoteUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(): Flow<List<RelatedUserRemoteData>?> {
        return withContext(Dispatchers.IO) {
            repository.getMyRelatedUserListFromRemote()
        }
    }
}
