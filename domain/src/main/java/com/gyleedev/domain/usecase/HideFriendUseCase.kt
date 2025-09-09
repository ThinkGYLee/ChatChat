package com.gyleedev.domain.usecase

import com.gyleedev.domain.model.ChangeRelationResult
import com.gyleedev.domain.model.RelatedUserLocalData
import com.gyleedev.domain.repository.UserRepository
import javax.inject.Inject

class HideFriendUseCase @Inject constructor(
    private val repository: UserRepository,
) {
    suspend operator fun invoke(friend: RelatedUserLocalData): ChangeRelationResult = repository.hideFriendRequest(friend)
}
