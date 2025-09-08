package com.gyleedev.domain.usecase

import com.gyleedev.domain.model.RelatedUserLocalData
import com.gyleedev.domain.repository.UserRepository
import javax.inject.Inject

class GetRelatedUserAndFavoriteDataUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(uid: String): RelatedUserLocalData {
        return repository.getFriendAndFavoriteByUid(uid)
    }
}
