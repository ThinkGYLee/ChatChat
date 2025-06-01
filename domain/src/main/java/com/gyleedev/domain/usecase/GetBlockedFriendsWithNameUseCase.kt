package com.gyleedev.domain.usecase

import com.gyleedev.chatchat.domain.repository.UserRepository
import javax.inject.Inject

class GetBlockedFriendsWithNameUseCase @Inject constructor(
    private val repository: UserRepository
) {
    // TODO fts4 관련 쿼리문 수정
    // operator fun invoke(query: String) = repository.getHideFriendsWithFullTextName(query)
    operator fun invoke(query: String) = repository.getBlockedFriendsWithName(query)
}
