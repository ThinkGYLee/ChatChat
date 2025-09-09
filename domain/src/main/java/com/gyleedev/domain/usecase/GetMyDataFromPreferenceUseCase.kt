package com.gyleedev.domain.usecase

import com.gyleedev.domain.repository.UserRepository
import javax.inject.Inject

class GetMyDataFromPreferenceUseCase @Inject constructor(
    private val repository: UserRepository,
) {
    operator fun invoke() = repository.getMyUserDataFromPreference()
}
