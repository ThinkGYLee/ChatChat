package com.gyleedev.domain.model

data class ChatRoomDataWithAllRelatedUsersAndMessage(
    val chatRoomLocalData: ChatRoomLocalData,
    val relatedUserLocalData: RelatedUserLocalData,
    val lastMessageData: MessageData
)
// view에서 lastMessage 매핑하는 용
