package com.gyleedev.domain.usecase

import com.gyleedev.domain.model.RelatedUserLocalData
import com.gyleedev.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRelatedUserAndFavoriteDataUseCase @Inject constructor(
    private val repository: UserRepository
) {
    operator fun invoke(uid: String): Flow<RelatedUserLocalData> {
        return repository.getFriendAndFavoriteByUid(uid)
    }
}
