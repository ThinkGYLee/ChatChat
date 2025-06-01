package com.gyleedev.domain.usecase

import com.gyleedev.chatchat.domain.repository.UserRepository
import javax.inject.Inject

class UpdateFriendInfoUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(uid: String) {
        repository.updateUserInfoByUid(uid)
    }
}
