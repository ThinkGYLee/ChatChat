package com.gyleedev.domain.usecase

import com.gyleedev.domain.model.LogInResult
import com.gyleedev.domain.repository.UserRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class LoginUseCaseTest {
    private lateinit var useCase: LoginUseCase

    @MockK
    lateinit var repository: UserRepository

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        useCase = LoginUseCase(repository)
    }

    @Test
    fun `로그인 테스트`() {
        val id = "id"
        val password = "password"

        val result = LogInResult.Success
        coEvery {
            repository.loginRequest(id, password)
        } returns flowOf(result)

        runTest {
            val answer = useCase.invoke(id, password).first()
            assertEquals(result, answer)

            coVerify {
                repository.loginRequest(id, password)
            }
        }
    }
}
