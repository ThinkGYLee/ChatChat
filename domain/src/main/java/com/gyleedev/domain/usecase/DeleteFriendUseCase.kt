package com.gyleedev.domain.usecase

import com.gyleedev.chatchat.domain.model.RelatedUserLocalData
import com.gyleedev.chatchat.domain.repository.UserRepository
import javax.inject.Inject

class DeleteFriendUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(friend: RelatedUserLocalData) =
        repository.deleteFriendRequest(friend)
}
