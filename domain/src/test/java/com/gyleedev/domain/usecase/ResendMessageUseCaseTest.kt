package com.gyleedev.domain.usecase

import com.gyleedev.domain.model.MessageData
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ResendMessageUseCaseTest {

    private lateinit var resendUseCase: ResendMessageUseCase

    @MockK
    lateinit var sendMessageToRemoteUseCase: SendMessageToRemoteUseCase

    @MockK
    lateinit var updateMessageStateUseCase: UpdateMessageStateUseCase

    @MockK
    lateinit var getMessageFromLocalUseCase: GetMessageFromLocalUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        resendUseCase = ResendMessageUseCase(
            sendMessageToRemoteUseCase,
            updateMessageStateUseCase,
            getMessageFromLocalUseCase
        )
    }

    @Test
    fun `네트워크가 실패 했을 때`() {
        runTest {
            val messageData = MessageData()
            val rid = 1L
            val networkState = false
            resendUseCase.invoke(messageData, rid, networkState)
            coVerify(exactly = 0) {
                getMessageFromLocalUseCase(any())
                sendMessageToRemoteUseCase(any())
                updateMessageStateUseCase(any(), any(), any())
            }
        }
    }
}
