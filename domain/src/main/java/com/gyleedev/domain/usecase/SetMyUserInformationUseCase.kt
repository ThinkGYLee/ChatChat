package com.gyleedev.domain.usecase

import com.gyleedev.domain.model.UserData
import com.gyleedev.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SetMyUserInformationUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(userData: UserData) {
        return withContext(Dispatchers.IO) {
            repository.setMyUserInformation(userData)
        }
    }
}
