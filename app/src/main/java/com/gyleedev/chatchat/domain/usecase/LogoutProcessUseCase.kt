package com.gyleedev.chatchat.domain.usecase

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class LogoutProcessUseCase @Inject constructor(
    private val logoutUseCase: LogoutUseCase,
    private val myUserResetUseCase: MyUserResetUseCase,
    private val friendResetUseCase: FriendResetUseCase,
    private val chatRoomResetUseCase: ChatRoomResetUseCase,
    private val messageResetUseCase: MessageResetUseCase
) {
    suspend operator fun invoke() {
        return withContext(Dispatchers.IO) {
            logoutUseCase()
            myUserResetUseCase()
            friendResetUseCase()
            chatRoomResetUseCase()
            messageResetUseCase()
        }
    }
}
