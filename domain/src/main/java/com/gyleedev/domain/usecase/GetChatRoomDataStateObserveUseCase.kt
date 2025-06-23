package com.gyleedev.domain.usecase

import com.gyleedev.domain.repository.ChatRoomRepository
import javax.inject.Inject

// TODO 로직관련은 레포지토리로 유스케이스는 깔끔하게 유지할것
class GetChatRoomDataStateObserveUseCase @Inject constructor(
    private val repository: ChatRoomRepository
) {
    operator fun invoke() = repository.currentState
}
