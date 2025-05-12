package com.gyleedev.chatchat.data.model

import com.google.gson.annotations.SerializedName
import com.gyleedev.chatchat.domain.MessageType

// 기본값 설정 안해주면 crash남
data class MessageRemoteData(
    @SerializedName("chatRoomId") val chatRoomId: String = "",
    @SerializedName("writer") val writer: String = "",
    @SerializedName("type") val type: MessageType = MessageType.Text,
    @SerializedName("comment") val comment: String = "",
    @SerializedName("time") val time: Long = 0L,
    @SerializedName("replyTo") val replyTo: String? = null,
    @SerializedName("replyComment") val replyComment: String? = null,
    @SerializedName("replyType") val replyType: MessageType? = null,
    @SerializedName("replyKey") val replyKey: Long? = null
)
