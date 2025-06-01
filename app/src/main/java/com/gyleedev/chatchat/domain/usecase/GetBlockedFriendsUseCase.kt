package com.gyleedev.chatchat.domain.usecase

import com.gyleedev.chatchat.domain.repository.UserRepository
import javax.inject.Inject

class GetBlockedFriendsUseCase @Inject constructor(
    private val repository: UserRepository
) {
    operator fun invoke() = repository.getBlockedFriends()
}
