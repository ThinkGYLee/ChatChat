package com.gyleedev.domain.usecase

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

class VerifyEmailRequestUseCaseTest {

    private lateinit var useCase: VerifyEmailRequestUseCase

    @MockK
    lateinit var repository: UserRepository

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        useCase = VerifyEmailRequestUseCase(repository)
    }

    @Test
    fun `이메일 인증 리퀘스트 테스트`() {
        val result = true
        coEvery {
            repository.verifyEmailRequest()
        } returns flowOf(result)

        runTest {
            val answer = useCase.invoke()

            assertEquals(result, answer)

            coVerify {
                repository.verifyEmailRequest()
            }
        }
    }
}
