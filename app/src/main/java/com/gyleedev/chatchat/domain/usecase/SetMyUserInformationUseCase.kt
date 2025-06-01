package com.gyleedev.chatchat.domain.usecase

import com.gyleedev.chatchat.domain.model.UserData
import com.gyleedev.chatchat.domain.repository.UserRepository
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
