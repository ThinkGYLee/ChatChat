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

// Repository에서 exception을 catch 해서 ChangeRelationResult.FAILURE를 리턴하기 때문에 예외처리 테스트 필요 없음
class DeleteFriendUseCaseTest {

    private lateinit var useCase: DeleteFriendUseCase

    @MockK
    lateinit var repository: UserRepository

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        useCase = DeleteFriendUseCase(repository)
    }

    @Test
    fun `친구_삭제_요청_성공`() {
        val friend = RelatedUserLocalData(uid = "uid")
        val expected = ChangeRelationResult.SUCCESS
        coEvery { repository.deleteFriendRequest(friend) } returns expected
        runTest {
            val result = useCase(friend)

            assertEquals(expected, result)

            coVerify(exactly = 1) {
                repository.deleteFriendRequest(friend)
            }
        }
    }

    @Test
    fun `친구_삭제_요청_실패`() {
        val friend = RelatedUserLocalData(uid = "uid")
        val expected = ChangeRelationResult.FAILURE
        coEvery { repository.deleteFriendRequest(friend) } returns expected
        runTest {
            val result = useCase(friend)

            assertEquals(expected, result)

            coVerify(exactly = 1) {
                repository.deleteFriendRequest(friend)
            }
        }
    }
}
