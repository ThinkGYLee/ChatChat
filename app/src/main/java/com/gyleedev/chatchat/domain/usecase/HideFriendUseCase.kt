package com.gyleedev.chatchat.domain.usecase

import com.gyleedev.chatchat.domain.model.RelatedUserLocalData
import com.gyleedev.chatchat.domain.repository.UserRepository
import javax.inject.Inject

class HideFriendUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(friend: RelatedUserLocalData) = repository.hideFriendRequest(friend)
}
