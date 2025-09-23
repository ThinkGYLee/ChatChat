package com.gyleedev.domain.usecase

import com.gyleedev.domain.model.ChangeRelationResult
import com.gyleedev.domain.model.RelatedUserLocalData
import com.gyleedev.domain.repository.UserRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class BlockRelatedUserUseCaseTest {

    private lateinit var useCase: BlockRelatedUserUseCase

    @MockK
    lateinit var repository: UserRepository

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        useCase = BlockRelatedUserUseCase(repository)
    }

    @Test
    fun `블락_릴레이션 성공 케이스`() {
        val friend = RelatedUserLocalData()
        val result = ChangeRelationResult.SUCCESS
        coEvery { repository.blockRelatedUserRequest(friend) } returns result

        runTest {
            val answer = useCase.invoke(friend)

            assertEquals(result, answer)

            coVerify(exactly = 1) {
                repository.blockRelatedUserRequest(friend)
            }
        }
    }

    @Test
    fun `블락_릴레이션 실패 케이스`() {
        val friend = RelatedUserLocalData()
        val result = ChangeRelationResult.FAILURE
        coEvery { repository.blockRelatedUserRequest(friend) } returns result

        runTest {
            val answer = useCase.invoke(friend)

            assertEquals(result, answer)

            coVerify(exactly = 1) {
                repository.blockRelatedUserRequest(friend)
            }
        }
    }

    @Test
    fun `블락_릴레이션 예외 발생 케이스`() {
        val friend = RelatedUserLocalData()
        val error = RuntimeException("test")
        coEvery { repository.blockRelatedUserRequest(friend) } throws error

        runTest {
            var thrown = false
            try {
                useCase.invoke(friend)
            } catch (e: Throwable) {
                thrown = true
                assertEquals(error.message, e.message)
            }

            assertEquals(true, thrown)

            coVerify(exactly = 1) {
                repository.blockRelatedUserRequest(friend)
            }
        }
    }
}
