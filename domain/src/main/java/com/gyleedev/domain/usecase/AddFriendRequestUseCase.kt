package com.gyleedev.domain.usecase

import com.gyleedev.domain.model.UserData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject

// TODO 로직관련은 레포지토리로 유스케이스는 깔끔하게 유지할것
class AddFriendRequestUseCase @Inject constructor(
    private val addFriendToRemoteUseCase: AddFriendToRemoteUseCase,
    private val addFriendToLocalUseCase: AddFriendToLocalUseCase,
) {
    operator fun invoke(userData: UserData): Flow<Boolean> = callbackFlow {
        withContext(Dispatchers.IO) {
            val remoteRequest = addFriendToRemoteUseCase(userData)
            if (remoteRequest) {
                val localRequest = addFriendToLocalUseCase(userData)
                if (localRequest) {
                    trySend(true)
                } else {
                    trySend(false)
                }
            } else {
                trySend(false)
            }
        }
        awaitClose()
    }
}
