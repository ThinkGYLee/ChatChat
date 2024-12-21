package com.gyleedev.chatchat.domain

data class ChatRoomDataWithFriend(
    val chatRoomLocalData: ChatRoomLocalData,
    val friendData: FriendData
)
//레포지토리에서 맵핑하는 용