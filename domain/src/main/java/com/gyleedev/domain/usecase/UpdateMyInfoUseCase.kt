package com.gyleedev.domain.usecase

import com.gyleedev.domain.model.UserData
import com.gyleedev.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UpdateMyInfoUseCase @Inject constructor(
    private val repository: UserRepository,
) {
    suspend operator fun invoke(user: UserData): Boolean = withContext(Dispatchers.IO) {
        repository.updateMyUserInfo(user).first()
    }
}
