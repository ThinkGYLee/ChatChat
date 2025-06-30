package com.gyleedev.domain.usecase

import android.os.Build
import androidx.annotation.RequiresApi
import com.gyleedev.domain.model.MessageData
import com.gyleedev.domain.model.MessageSendState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.time.Instant
import javax.inject.Inject

/**
 *  1. 네트워크가 실패 했을 때
 *  2. 네트워크가 성공 했을 때
 *    2-2. 메시지 아이디가 있을 때
 *      2-2.1. 메시지 보내고 성공한 경우 업데이트
 *      2-2.2. 메시지 보내고 실패한 경우 업데이트가 아님
 */
class ResendMessageUseCase @Inject constructor(
    private val sendMessageToRemoteUseCase: SendMessageToRemoteUseCase,
    private val updateMessageStateUseCase: UpdateMessageStateUseCase,
    private val getMessageFromLocalUseCase: GetMessageFromLocalUseCase
) {
    @RequiresApi(Build.VERSION_CODES.O)
    suspend operator fun invoke(
        messageData: MessageData,
        rid: Long,
        networkState: Boolean,
        time: Long = Instant.now().toEpochMilli()
    ) {
        if (!networkState) {
            return
        }
        withContext(Dispatchers.IO) {
            val messageId = getMessageFromLocalUseCase(messageData).first().messageId
            val request = try {
                sendMessageToRemoteUseCase(messageData).first()
            } catch (e: Throwable) {
                e.printStackTrace()
                // TODO 처리
            }
            if (request is MessageSendState) {
                val message = messageData.copy(
                    messageSendState = request,
                    time = time
                )
                updateMessageStateUseCase(messageId, rid, message)
            }
        }
    }
}
