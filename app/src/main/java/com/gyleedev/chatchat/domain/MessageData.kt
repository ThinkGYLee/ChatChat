package com.gyleedev.chatchat.domain

import com.google.gson.annotations.SerializedName
import com.gyleedev.chatchat.data.model.MessageRemoteData

// 기본값 설정 안해주면 crash남
data class MessageData(
    @SerializedName("chatRoomId") val chatRoomId: String = "",
    @SerializedName("writer") val writer: String = "",
    @SerializedName("comment") val comment: String = "",
    @SerializedName("time") val time: Long = 0L,
    @SerializedName("messageSendState") val messageSendState: MessageSendState = MessageSendState.COMPLETE
)

fun MessageData.toRemoteModel(): MessageRemoteData {
    return MessageRemoteData(
        chatRoomId = chatRoomId,
        writer = writer,
        comment = comment,
        time = time
    )
}
