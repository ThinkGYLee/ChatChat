package com.gyleedev.chatchat.domain

import com.google.gson.annotations.SerializedName

// 기본값 설정 안해주면 crash남
data class MessageData(
    @SerializedName("chatRoomId") val chatRoomId: String = "",
    @SerializedName("writer") val writer: String = "",
    @SerializedName("comment") val comment: String = "",
    @SerializedName("time") val time: Long = 0L
)
