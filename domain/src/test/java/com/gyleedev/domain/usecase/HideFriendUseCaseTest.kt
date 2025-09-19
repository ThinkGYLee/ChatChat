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

class HideFriendUseCaseTest {
    private lateinit var useCase: HideFriendUseCase

    @MockK
    lateinit var repository: UserRepository

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        useCase = HideFriendUseCase(repository)
    }

    @Test
    fun `친구 숨기기 성공 테스트`() {
        val friend = RelatedUserLocalData()
        val result = ChangeRelationResult.SUCCESS

        coEvery {
            repository.hideFriendRequest(friend)
        } returns result

        runTest {
            val answer = useCase.invoke(friend)

            assertEquals(result, answer)

            coVerify {
                repository.hideFriendRequest(friend)
            }
        }
    }

    @Test
    fun `친구 숨기기 실패 테스트`() {
        val friend = RelatedUserLocalData()
        val result = ChangeRelationResult.FAILURE

        coEvery {
            repository.hideFriendRequest(friend)
        } returns result

        runTest {
            val answer = useCase.invoke(friend)

            assertEquals(result, answer)

            coVerify {
                repository.hideFriendRequest(friend)
            }
        }
    }

    @Test
    fun `친구 숨기기 예외 테스트`() {
        val friend = RelatedUserLocalData()
        val result = RuntimeException("test")

        coEvery {
            repository.hideFriendRequest(friend)
        } throws result

        runTest {
            var thrown = false
            try {
                useCase.invoke(friend)
            } catch (e: Throwable) {
                thrown = true
                assertEquals(result.message, e.message)
            }
            assertEquals(true, thrown)

            coVerify {
                repository.hideFriendRequest(friend)
            }
        }
    }
}
