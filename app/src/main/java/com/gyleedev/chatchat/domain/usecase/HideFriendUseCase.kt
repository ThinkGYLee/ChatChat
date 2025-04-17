package com.gyleedev.chatchat.domain.usecase

import com.gyleedev.chatchat.data.repository.UserRepository
import com.gyleedev.chatchat.domain.RelatedUserLocalData
import javax.inject.Inject

class HideFriendUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(friend: RelatedUserLocalData) = repository.hideFriendRequest(friend)
}
