package com.gyleedev.chatchat.domain.usecase

import com.gyleedev.chatchat.data.repository.UserRepository
import javax.inject.Inject

class GetMyDataFromPreferenceUseCase @Inject constructor(
    private val repository: UserRepository
) {
    operator fun invoke() = repository.getMyUserDataFromPreference()
}
