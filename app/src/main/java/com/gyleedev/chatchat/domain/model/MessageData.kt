package com.gyleedev.chatchat.domain.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

// 기본값 설정 안해주면 crash남
@Parcelize
data class MessageData(
    @SerializedName("chatRoomId") val chatRoomId: String = "",
    @SerializedName("writer") val writer: String = "",
    @SerializedName("comment") val comment: String = "",
    @SerializedName("type") val type: MessageType = MessageType.Text,
    @SerializedName("time") val time: Long = 0L,
    @SerializedName("messageSendState") val messageSendState: MessageSendState = MessageSendState.COMPLETE,
    @SerializedName("replyTo") val replyTo: String? = null, // 답장 상대의 uid
    @SerializedName("replyComment") val replyComment: String? = null, // 답장 상대의 메시지 내용
    @SerializedName("replyType") val replyType: MessageType? = null, // 답장 상대의 메시지 타입
    @SerializedName("replyKey") val replyKey: Long? = null // 로컬 db Key 값으로 쓸것 (고유값인 time)
) : Parcelable

fun MessageData.toRemoteModel(): MessageRemoteData {
    return MessageRemoteData(
        chatRoomId = chatRoomId,
        writer = writer,
        comment = comment,
        type = type,
        time = time,
        replyTo = replyTo,
        replyType = replyType,
        replyComment = replyComment,
        replyKey = replyKey
    )
}
