package com.gyleedev.chatchat.domain.usecase

import com.gyleedev.chatchat.data.repository.UserRepository
import com.gyleedev.chatchat.domain.RelatedUserLocalData
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRelatedUserAndFavoriteDataUseCase @Inject constructor(
    private val repository: UserRepository
) {
    operator fun invoke(uid: String): Flow<RelatedUserLocalData> {
        return repository.getFriendAndFavoriteByUid(uid)
    }
}
