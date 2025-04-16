package com.gyleedev.chatchat.domain.usecase

import com.gyleedev.chatchat.data.repository.UserRepository
import com.gyleedev.chatchat.domain.RelatedUserLocalData
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFriendDataUseCase @Inject constructor(
    private val repository: UserRepository
) {
    operator fun invoke(friend: String): Flow<RelatedUserLocalData> {
        return repository.getFriendById(friend)
    }
}
