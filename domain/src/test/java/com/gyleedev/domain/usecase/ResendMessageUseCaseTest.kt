package com.gyleedev.domain.usecase

import com.gyleedev.domain.model.MessageData
import com.gyleedev.domain.model.MessageSendState
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.runs
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.Instant

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
            // verify 에서 exactly = 0을 지정할 경우 호출 하지 않는 다는 것을 체크
            coVerify(exactly = 0) {
                getMessageFromLocalUseCase(any())
                sendMessageToRemoteUseCase(any())
                updateMessageStateUseCase(any(), any(), any())
            }
        }
    }

    @Test
    fun `네트워크가 성공 했을 때 메시지를 보내고 상태 업데이트를 성공하는 경우`() {
        val messageData = MessageData()
        val rid = 1L
        val networkState = true
        val sendState = MessageSendState.COMPLETE
        val time = Instant.now().toEpochMilli()
        coEvery { getMessageFromLocalUseCase(messageData) } returns flowOf(messageData)
        coEvery { sendMessageToRemoteUseCase(messageData) } returns flowOf(sendState)
        val message = messageData.copy(
            messageSendState = sendState,
            time = time
        )
        coEvery { updateMessageStateUseCase(messageData.messageId, rid, message) } just runs
        runTest {
            resendUseCase.invoke(messageData, rid, networkState, time)

            coVerifyOrder {
                getMessageFromLocalUseCase(messageData)
                sendMessageToRemoteUseCase(messageData)
                updateMessageStateUseCase(messageData.messageId, rid, message)
            }
        }
    }

    @Test
    fun `네트워크가 성공 했을 때 메시지를 보내고 상태 업데이트를 호출하지 않는 경우`() {
        val messageData = MessageData()
        val rid = 1L
        val networkState = true
        val sendState = MessageSendState.COMPLETE
        val time = Instant.now().toEpochMilli()
        coEvery { getMessageFromLocalUseCase(messageData) } returns flowOf(messageData)
        coEvery { sendMessageToRemoteUseCase(messageData) } throws Exception("test")
        val message = messageData.copy(
            messageSendState = sendState,
            time = time
        )
        coEvery { updateMessageStateUseCase(messageData.messageId, rid, message) } just runs
        runTest {
            resendUseCase.invoke(messageData, rid, networkState, time)

            coVerifyOrder {
                getMessageFromLocalUseCase(messageData)
                sendMessageToRemoteUseCase(messageData)
            }

            coVerify(exactly = 0) {
                updateMessageStateUseCase(any(), any(), any())
            }
        }
    }
}
