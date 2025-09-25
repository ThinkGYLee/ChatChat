package com.gyleedev.domain.usecase

import com.gyleedev.domain.model.RelatedUserRemoteData
import com.gyleedev.domain.repository.UserRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

// Refactor
// 리턴타입이 Unit인데 실패시에 처리가 없음
// 더 상단(ViewModel 단에서 구조변경을 고민해야함
class AddMyRelatedUsersUseCaseTest {

    private lateinit var useCase: AddMyRelatedUsersUseCase

    @MockK
    lateinit var repository: UserRepository

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        useCase = AddMyRelatedUsersUseCase(repository)
    }

    @Test
    fun `릴레이션_로컬_삽입_성공`() = runTest {
        val friends = listOf(RelatedUserRemoteData(uid = "uid"))
        coEvery { repository.insertMyRelationsToLocal(friends) } returns Unit

        useCase(friends)

        coVerify(exactly = 1) {
            repository.insertMyRelationsToLocal(friends)
        }
    }

    @Test
    fun `릴레이션_로컬_삽입_예외`() = runTest {
        val friends = listOf(RelatedUserRemoteData(uid = "uid"))
        val error = RuntimeException("error")
        coEvery { repository.insertMyRelationsToLocal(friends) } throws error

        var thrown = false

        try {
            useCase(friends)
        } catch (e: Throwable) {
            thrown = true
            assertEquals(error.message, e.message)
        }

        assertEquals(true, thrown)

        coVerify(exactly = 1) {
            repository.insertMyRelationsToLocal(friends)
        }
    }
}
