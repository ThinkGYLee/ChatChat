package com.gyleedev.domain.usecase

import com.gyleedev.domain.model.RelatedUserLocalData
import com.gyleedev.domain.repository.UserRepository
import javax.inject.Inject

class UserToFriendUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(friend: RelatedUserLocalData) =
        repository.userToFriendRequest(friend)
}
