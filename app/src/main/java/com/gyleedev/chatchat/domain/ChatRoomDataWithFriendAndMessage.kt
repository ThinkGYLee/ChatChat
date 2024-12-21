package com.gyleedev.chatchat.domain

data class ChatRoomDataWithFriendAndMessage(
    val chatRoomLocalData: ChatRoomLocalData,
    val friendData: FriendData,
    val lastMessageData: MessageData
)
//view에서 lastMessage 매핑하는 용
