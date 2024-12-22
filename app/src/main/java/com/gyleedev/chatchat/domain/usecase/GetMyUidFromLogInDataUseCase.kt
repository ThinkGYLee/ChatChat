package com.gyleedev.chatchat.domain.usecase

import com.gyleedev.chatchat.data.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class GetMyUidFromLogInDataUseCase @Inject constructor(
    private val repository: UserRepository
) {
    operator fun invoke(): Flow<String?> {
        return flow {
            val uid = repository.getMyUidFromLogInData()
            emit(uid)
        }.flowOn(Dispatchers.IO)
    }
}
