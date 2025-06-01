package com.gyleedev.domain.usecase

import com.gyleedev.domain.model.RelatedUserLocalData
import com.gyleedev.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFriendDataUseCase @Inject constructor(
    private val repository: UserRepository
) {
    operator fun invoke(friend: String): Flow<RelatedUserLocalData> {
        return repository.getFriendById(friend)
    }
}
