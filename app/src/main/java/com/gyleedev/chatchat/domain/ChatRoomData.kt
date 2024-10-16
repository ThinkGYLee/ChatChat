package com.gyleedev.chatchat.domain

import com.google.gson.annotations.SerializedName

// 기본값 설정 안해주면 crash남
data class ChatRoomData(
    @SerializedName("id") val uid: String = "",
    @SerializedName("user1") val user1: String = "",
    @SerializedName("user2") val user2: String = "",
    @SerializedName("messageList") val messageList: List<MessageData> = emptyList()
)
