package com.gyleedev.chatchat.domain.usecase

import com.gyleedev.chatchat.data.repository.UserRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class UpdateFriendListUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke() {
        val list = repository.getFriendListFromLocal().first()
        list.forEach {
            repository.updateFriendInfoWithFriendEntity(it)
        }
    }
}
