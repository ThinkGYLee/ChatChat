package com.gyleedev.domain.usecase

import com.gyleedev.chatchat.domain.model.LogInResult
import com.gyleedev.chatchat.domain.model.LogInState
import com.gyleedev.chatchat.domain.model.SearchUserResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class LoginProcessUseCase @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val getUserDataUseCase: GetUserDataUseCase,
    private val setMyUserInformationUseCase: SetMyUserInformationUseCase
) {
    suspend operator fun invoke(id: String, password: String): LogInState {
        return withContext(Dispatchers.IO) {
            val logInResult = loginUseCase(id, password).first()
            if (logInResult is LogInResult.Failure) {
                LogInState.Failure(
                    message = logInResult.message
                )
            } else {
                val searchUser = getUserDataUseCase(id).first()
                if (searchUser is SearchUserResult.Success) {
                    setMyUserInformationUseCase(searchUser.user)
                    LogInState.Success(
                        userData = searchUser.user
                    )
                } else {
                    LogInState.Failure(
                        message = "cant get user data"
                    )
                }
            }
        }
    }
}
