package com.gyleedev.domain.usecase

import com.gyleedev.domain.repository.UserRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class UpdateRelatedUserListUseCase @Inject constructor(
    private val repository: UserRepository,
) {
    suspend operator fun invoke() {
        val list = repository.getRelatedUserListFromLocal().first()
        list.forEach {
            repository.updateRelatedUserInfoWithUserEntity(it)
        }
    }
}
