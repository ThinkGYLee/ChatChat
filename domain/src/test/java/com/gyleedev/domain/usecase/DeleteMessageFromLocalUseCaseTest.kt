package com.gyleedev.domain.usecase

import com.gyleedev.domain.repository.MessageRepository
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

// Unit 리턴되는 함수기 때문에 값을 비교할 필요는 없음
// 예외도 로컬을 찌르는 함수기 때문에 체크할 필요 없음
class DeleteMessageFromLocalUseCaseTest {

    private lateinit var useCase: DeleteMessageFromLocalUseCase

    @MockK
    lateinit var repository: MessageRepository

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        useCase = DeleteMessageFromLocalUseCase(repository)
    }

    @Test
    fun `로컬_메시지_삭제_성공`() = runTest {
        val messageId = 1L

        val result = useCase(messageId)

        assertEquals(Unit, result)

        coVerify(exactly = 1) {
            repository.deleteLocalMessage(messageId)
        }
    }
}
