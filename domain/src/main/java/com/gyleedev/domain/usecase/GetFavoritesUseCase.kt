package com.gyleedev.domain.usecase

import com.gyleedev.domain.repository.UserRepository
import javax.inject.Inject

class GetFavoritesUseCase @Inject constructor(
    private val repository: UserRepository,
) {
    operator fun invoke() = repository.getFavorites()
}
