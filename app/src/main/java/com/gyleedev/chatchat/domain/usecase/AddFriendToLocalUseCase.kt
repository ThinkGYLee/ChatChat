package com.gyleedev.chatchat.domain.usecase

import com.gyleedev.chatchat.data.repository.UserRepository
import com.gyleedev.chatchat.domain.UserData
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AddFriendToLocalUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(userData: UserData): Flow<Boolean> =
        repository.insertFriendToLocal(userData)
}
