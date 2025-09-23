package com.gyleedev.domain.usecase

import com.gyleedev.domain.model.UserData
import com.gyleedev.domain.repository.UserRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class UpdateMyInfoUseCaseTest {
    private lateinit var useCase: UpdateMyInfoUseCase

    @MockK
    lateinit var repository: UserRepository

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        useCase = UpdateMyInfoUseCase(repository)
    }

    @Test
    fun `내정보 업데이트 성공 테스트`() {
        val user = UserData()
        val result = true

        coEvery {
            repository.updateMyUserInfo(user)
        } returns flowOf(result)

        runTest {
            val answer = useCase.invoke(user)

            assertEquals(result, answer)

            coVerify(exactly = 1) {
                repository.updateMyUserInfo(user)
            }
        }
    }

    @Test
    fun `내정보 업데이트 실패 테스트`() {
        val user = UserData()
        val result = false

        coEvery {
            repository.updateMyUserInfo(user)
        } returns flowOf(result)

        runTest {
            val answer = useCase.invoke(user)

            assertEquals(result, answer)

            coVerify(exactly = 1) {
                repository.updateMyUserInfo(user)
            }
        }
    }
}
