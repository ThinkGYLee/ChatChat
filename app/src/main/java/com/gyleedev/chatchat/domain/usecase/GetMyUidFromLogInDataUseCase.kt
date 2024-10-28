package com.gyleedev.chatchat.domain.usecase

import com.gyleedev.chatchat.data.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetMyUidFromLogInDataUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(): String? {
        return withContext(Dispatchers.IO) {
            repository.getMyUidFromLogInData()
        }
    }
}
