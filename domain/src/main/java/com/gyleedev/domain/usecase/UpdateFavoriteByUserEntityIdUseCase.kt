package com.gyleedev.domain.usecase

import com.gyleedev.domain.model.RelatedUserLocalData
import com.gyleedev.domain.repository.UserRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class UpdateFavoriteByUserEntityIdUseCase @Inject constructor(
    private val repository: UserRepository,
) {
    suspend operator fun invoke(relatedUserLocalData: RelatedUserLocalData): Boolean = repository.updateUserAndFavorite(relatedUserLocalData).first()
}
