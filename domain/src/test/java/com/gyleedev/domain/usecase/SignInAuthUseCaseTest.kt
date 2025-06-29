package com.gyleedev.domain.usecase

import com.gyleedev.domain.model.UserData
import com.gyleedev.domain.repository.UserRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class SignInAuthUseCaseTest {
    private lateinit var useCase: SignInAuthUseCase

    @MockK
    lateinit var repository: UserRepository

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        useCase = SignInAuthUseCase(repository)
    }

    @Test
    fun `회원 가입 인증`() {
        val id = "id"
        val password = "password"
        val nickname = "nickname"
        val result = flowOf(UserData())

        coEvery {
            repository.signInUser(id, password, nickname)
        } returns result

        runTest {
            val answer = useCase.invoke(id, password, nickname)

            assertEquals(result, answer)

            coVerify {
                repository.signInUser(id, password, nickname)
            }
        }
    }
}
