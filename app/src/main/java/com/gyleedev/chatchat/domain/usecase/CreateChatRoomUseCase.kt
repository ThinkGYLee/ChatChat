package com.gyleedev.chatchat.domain.usecase

import com.gyleedev.chatchat.data.repository.UserRepository
import javax.inject.Inject

class CreateChatRoomUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke() = repository.createChatRoomData()
}
