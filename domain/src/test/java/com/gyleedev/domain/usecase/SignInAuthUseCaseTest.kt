package com.gyleedev.domain.usecase

import com.gyleedev.domain.model.UserData
import com.gyleedev.domain.repository.UserRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

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
    fun `회원 가입 인증 성공 케이스`() {
        val id = "id"
        val password = "password"
        val nickname = "nickname"
        val result = flowOf(UserData())

        coEvery {
            repository.signInUser(id, password, nickname)
        } returns result

        runTest {
            val answer = useCase.invoke(id, password, nickname)

            assertEquals(result.first(), answer.first())

            coVerify(exactly = 1) {
                repository.signInUser(id, password, nickname)
            }
        }
    }

    @Test
    fun `회원 가입 인증 실패 케이스`() {
        val id = "id"
        val password = "password"
        val nickname = "nickname"
        val result = flowOf(null)

        coEvery {
            repository.signInUser(id, password, nickname)
        } returns result

        runTest {
            val answer = useCase.invoke(id, password, nickname)

            assertEquals(result.first(), answer.first())

            coVerify(exactly = 1) {
                repository.signInUser(id, password, nickname)
            }
        }
    }

    @Test
    fun `회원 가입 인증 예외 케이스`() {
        val id = "id"
        val password = "password"
        val nickname = "nickname"
        val error = RuntimeException("test")

        coEvery {
            repository.signInUser(id, password, nickname)
        } throws error

        runTest {
            var thrown = false
            try {
                repository.signInUser(id, password, nickname)
            } catch (e: Throwable) {
                thrown = true
                assertEquals(error.message, e.message)
            }

            assertEquals(true, thrown)

            coVerify(exactly = 1) {
                repository.signInUser(id, password, nickname)
            }
        }
    }
}
