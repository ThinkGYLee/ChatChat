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

class AddFriendToRemoteUseCaseTest {

    private lateinit var useCase: AddFriendToRemoteUseCase

    @MockK
    lateinit var repository: UserRepository

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        useCase = AddFriendToRemoteUseCase(repository)
    }

    @Test
    fun `원격_친구_추가_성공`() = runTest {
        val user = UserData(uid = "uid")
        val result = flowOf(true)
        coEvery { repository.addRelatedUserToRemote(user) } returns result

        val answer = useCase(user)

        assertEquals(result.first(), answer)

        coVerify(exactly = 1) {
            repository.addRelatedUserToRemote(user)
        }
    }

    @Test
    fun `원격_친구_추가_실패`() = runTest {
        val user = UserData(uid = "uid")
        val result = flowOf(false)
        coEvery { repository.addRelatedUserToRemote(user) } returns result

        val answer = useCase(user)

        assertEquals(result.first(), answer)

        coVerify(exactly = 1) {
            repository.addRelatedUserToRemote(user)
        }
    }

    @Test
    fun `원격_친구_추가_예외`() = runTest {
        val user = UserData(uid = "uid")
        val error = RuntimeException("error")
        coEvery { repository.addRelatedUserToRemote(user) } throws error

        var thrown = false

        try {
            useCase(user)
        } catch (e: Throwable) {
            thrown = true
            assertEquals(error.message, e.message)
        }

        assertEquals(true, thrown)

        coVerify(exactly = 1) {
            repository.addRelatedUserToRemote(user)
        }
    }
}
