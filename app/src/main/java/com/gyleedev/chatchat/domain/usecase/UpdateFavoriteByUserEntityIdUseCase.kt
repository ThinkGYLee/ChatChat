package com.gyleedev.chatchat.domain.usecase

import com.gyleedev.chatchat.data.repository.UserRepository
import com.gyleedev.chatchat.domain.RelatedUserLocalData
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class UpdateFavoriteByUserEntityIdUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(relatedUserLocalData: RelatedUserLocalData): Boolean {
        return repository.updateUserAndFavorite(relatedUserLocalData).first()
    }
}
