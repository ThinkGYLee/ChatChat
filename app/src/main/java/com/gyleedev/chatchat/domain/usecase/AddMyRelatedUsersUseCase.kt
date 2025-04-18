package com.gyleedev.chatchat.domain.usecase

import com.gyleedev.chatchat.data.model.RelatedUserRemoteData
import com.gyleedev.chatchat.data.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AddMyRelatedUsersUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(friends: List<RelatedUserRemoteData>) {
        return withContext(Dispatchers.IO) {
            repository.insertMyRelationsToLocal(friends)
        }
    }
}
