package com.gyleedev.domain.usecase

import com.gyleedev.domain.model.MessageData
import com.gyleedev.domain.model.ProcessResult
import com.gyleedev.domain.repository.MessageRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class DeleteMessageUseCaseTest {

    private lateinit var useCase: DeleteMessageUseCase

    @MockK
    lateinit var repository: MessageRepository

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        useCase = DeleteMessageUseCase(repository)
    }

    @Test
    fun `메시지_삭제_요청_성공`() = runTest {
        val message = MessageData(messageId = 1L)
        val expected = ProcessResult.Success
        coEvery { repository.deleteMessageRequest(message) } returns flowOf(expected)

        val result = useCase(message)

        assertEquals(expected, result)

        coVerify(exactly = 1) {
            repository.deleteMessageRequest(message)
        }
    }

    @Test
    fun `메시지_삭제_요청_실패`() = runTest {
        val message = MessageData(messageId = 1L)
        val expected = ProcessResult.Failure
        coEvery { repository.deleteMessageRequest(message) } returns flowOf(expected)

        val result = useCase(message)

        assertEquals(expected, result)

        coVerify(exactly = 1) {
            repository.deleteMessageRequest(message)
        }
    }

    @Test
    fun `메시지_삭제_요청_예외`() = runTest {
        val message = MessageData(messageId = 1L)
        val error = RuntimeException("error")
        coEvery { repository.deleteMessageRequest(message) } throws error

        var thrown = false

        try {
            useCase(message)
        } catch (e: Throwable) {
            thrown = true
            assertEquals(error.message, e.message)
        }

        assertEquals(true, thrown)

        coVerify(exactly = 1) {
            repository.deleteMessageRequest(message)
        }
    }
}
