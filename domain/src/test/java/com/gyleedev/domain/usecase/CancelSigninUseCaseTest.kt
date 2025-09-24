package com.gyleedev.domain.usecase

import com.gyleedev.domain.repository.UserRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class CancelSigninUseCaseTest {

    private lateinit var useCase: CancelSigninUseCase

    @MockK
    lateinit var repository: UserRepository

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        useCase = CancelSigninUseCase(repository)
    }

    @Test
    fun `회원가입_취소_성공`() = runTest {
        val result = true
        coEvery { repository.cancelSigninRequest() } returns result

        val answer = useCase()

        assertEquals(result, answer)

        coVerify(exactly = 1) {
            repository.cancelSigninRequest()
        }
    }

    @Test
    fun `회원가입_취소_실패`() = runTest {
        val result = false
        coEvery { repository.cancelSigninRequest() } returns result

        val answer = useCase()

        assertEquals(result, answer)

        coVerify(exactly = 1) {
            repository.cancelSigninRequest()
        }
    }

    @Test
    fun `회원가입_취소_예외`() = runTest {
        val error = RuntimeException("error")
        coEvery { repository.cancelSigninRequest() } throws error

        var thrown = false

        try {
            useCase()
        } catch (e: Throwable) {
            thrown = true
            assertEquals(error.message, e.message)
        }

        assertEquals(true, thrown)

        coVerify(exactly = 1) {
            repository.cancelSigninRequest()
        }
    }
}
