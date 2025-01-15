package com.gyleedev.chatchat.domain.usecase

import com.gyleedev.chatchat.data.repository.UserRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class UpdateFriendInfoUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke() {

    }
}
