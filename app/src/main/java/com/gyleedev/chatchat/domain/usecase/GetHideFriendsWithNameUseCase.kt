package com.gyleedev.chatchat.domain.usecase

import com.gyleedev.chatchat.data.repository.UserRepository
import javax.inject.Inject

class GetHideFriendsWithNameUseCase @Inject constructor(
    private val repository: UserRepository
) {
    operator fun invoke(query: String) = repository.getHideFriendsWithName(query)
}
