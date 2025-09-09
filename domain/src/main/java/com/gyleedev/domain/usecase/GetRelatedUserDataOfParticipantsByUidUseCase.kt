package com.gyleedev.domain.usecase

import com.gyleedev.domain.repository.UserRepository
import javax.inject.Inject

class GetRelatedUserDataOfParticipantsByUidUseCase @Inject constructor(
    private val repository: UserRepository,
) {
    suspend operator fun invoke(
        uidList: List<String>,
    ) = repository.getUsersByUid(uidList)
}
