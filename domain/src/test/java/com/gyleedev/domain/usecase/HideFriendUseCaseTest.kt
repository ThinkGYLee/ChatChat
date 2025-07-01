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
    fun `친구 숨기기 기능 테스트`() {
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
}
