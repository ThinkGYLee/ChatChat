package com.gyleedev.chatchat.domain.usecase

import com.gyleedev.chatchat.data.repository.MessageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UploadImageToRemoteUseCase @Inject constructor(
    private val repository: MessageRepository
) {
    operator fun invoke(uid: String): Flow<String> =
        repository.uploadImageToRemote(uid)
}
