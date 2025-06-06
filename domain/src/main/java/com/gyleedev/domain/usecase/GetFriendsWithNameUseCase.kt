package com.gyleedev.domain.usecase

import com.gyleedev.domain.repository.UserRepository
import javax.inject.Inject

class GetFriendsWithNameUseCase @Inject constructor(
    private val repository: UserRepository
) {
    operator fun invoke(query: String) = repository.getFriendsWithName(query)
}
