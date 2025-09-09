package com.gyleedev.domain.usecase

import com.gyleedev.domain.model.RelatedUserRemoteData
import com.gyleedev.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AddMyRelatedUsersUseCase @Inject constructor(
    private val repository: UserRepository,
) {
    suspend operator fun invoke(friends: List<RelatedUserRemoteData>) = withContext(Dispatchers.IO) {
        repository.insertMyRelationsToLocal(friends)
    }
}
