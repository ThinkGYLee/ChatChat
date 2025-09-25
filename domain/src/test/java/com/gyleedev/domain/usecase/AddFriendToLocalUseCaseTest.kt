package com.gyleedev.domain.usecase

import com.gyleedev.domain.model.UserData
import com.gyleedev.domain.repository.UserRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class AddFriendToLocalUseCaseTest {

    private lateinit var useCase: AddFriendToLocalUseCase

    @MockK
    lateinit var repository: UserRepository

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        useCase = AddFriendToLocalUseCase(repository)
    }

    @Test
    fun `로컬_친구_추가_성공`() = runTest {
        val user = UserData(uid = "uid")
        val result = flowOf(true)
        coEvery { repository.insertFriendToLocal(user) } returns result

        val answer = useCase(user)

        assertEquals(answer, result.first())

        coVerify(exactly = 1) {
            repository.insertFriendToLocal(user)
        }
    }

    @Test
    fun `로컬_친구_추가_실패`() = runTest {
        val user = UserData(uid = "uid")
        val result = flowOf(false)
        coEvery { repository.insertFriendToLocal(user) } returns result

        val answer = useCase(user)

        assertEquals(answer, result.first())

        coVerify(exactly = 1) {
            repository.insertFriendToLocal(user)
        }
    }
}
