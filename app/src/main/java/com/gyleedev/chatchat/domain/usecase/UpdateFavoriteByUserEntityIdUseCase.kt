package com.gyleedev.chatchat.domain.usecase

import com.gyleedev.chatchat.domain.model.RelatedUserLocalData
import com.gyleedev.chatchat.domain.repository.UserRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class UpdateFavoriteByUserEntityIdUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(relatedUserLocalData: RelatedUserLocalData): Boolean {
        return repository.updateUserAndFavorite(relatedUserLocalData).first()
    }
}
