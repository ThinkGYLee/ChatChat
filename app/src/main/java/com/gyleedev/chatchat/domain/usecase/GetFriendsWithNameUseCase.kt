package com.gyleedev.chatchat.domain.usecase

import com.gyleedev.chatchat.domain.repository.UserRepository
import javax.inject.Inject

class GetFriendsWithNameUseCase @Inject constructor(
    private val repository: UserRepository
) {
    operator fun invoke(query: String) = repository.getFriendsWithName(query)
}
