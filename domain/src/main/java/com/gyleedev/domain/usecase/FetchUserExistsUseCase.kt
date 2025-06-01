package com.gyleedev.domain.usecase

import com.gyleedev.chatchat.domain.repository.UserRepository
import javax.inject.Inject

class FetchUserExistsUseCase @Inject constructor(
    private val repository: UserRepository
) {
    operator fun invoke() = repository.fetchUserExists()
}
