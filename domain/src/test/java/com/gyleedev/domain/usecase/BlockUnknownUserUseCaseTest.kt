package com.gyleedev.domain.usecase

import com.gyleedev.domain.model.ChangeRelationResult
import com.gyleedev.domain.model.UserData
import com.gyleedev.domain.repository.UserRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class BlockUnknownUserUseCaseTest {

    private lateinit var useCase: BlockUnknownUserUseCase

    @MockK
    lateinit var repository: UserRepository

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        useCase = BlockUnknownUserUseCase(repository)
    }

    @Test
    fun `알수없는_사용자_차단_성공`() = runTest {
        val userData = UserData(uid = "unknown")
        val result = ChangeRelationResult.SUCCESS
        coEvery { repository.blockUnknownUserRequest(userData) } returns result

        val answer = useCase(userData)

        assertEquals(result, answer)

        coVerify(exactly = 1) {
            repository.blockUnknownUserRequest(userData)
        }
    }

    @Test
    fun `알수없는_사용자_차단_실패`() = runTest {
        val userData = UserData(uid = "unknown")
        val result = ChangeRelationResult.FAILURE
        coEvery { repository.blockUnknownUserRequest(userData) } returns result

        val answer = useCase(userData)

        assertEquals(result, answer)

        coVerify(exactly = 1) {
            repository.blockUnknownUserRequest(userData)
        }
    }

    @Test
    fun `알수없는_사용자_차단_예외`() = runTest {
        val userData = UserData(uid = "unknown")
        val error = RuntimeException("error")
        coEvery { repository.blockUnknownUserRequest(userData) } throws error

        var thrown = false

        try {
            useCase(userData)
        } catch (e: Throwable) {
            thrown = true
            assertEquals(error.message, e.message)
        }

        assertEquals(true, thrown)

        coVerify(exactly = 1) {
            repository.blockUnknownUserRequest(userData)
        }
    }
}
