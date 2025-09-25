package com.gyleedev.domain.usecase

import com.gyleedev.domain.repository.UserRepository
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class CheckVerifiedUseCaseTest {

    private lateinit var useCase: CheckVerifiedUseCase

    @MockK
    lateinit var repository: UserRepository

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        useCase = CheckVerifiedUseCase(repository)
    }

    @Test
    fun `인증_여부_확인_성공`() {
        val result = flowOf(true)
        every { repository.checkUserVerified() } returns result
        runTest {
            val answer = useCase()

            assertEquals(result.first(), answer)

            verify(exactly = 1) {
                repository.checkUserVerified()
            }
        }
    }

    @Test
    fun `인증_여부_확인_예외`() {
        val result = RuntimeException("error")
        every { repository.checkUserVerified() } throws result
        runTest {
            var thrown = false

            try {
                useCase()
            } catch (e: Throwable) {
                thrown = true
                assertEquals(result.message, e.message)
            }

            assertEquals(true, thrown)

            verify(exactly = 1) {
                repository.checkUserVerified()
            }
        }
    }
}
