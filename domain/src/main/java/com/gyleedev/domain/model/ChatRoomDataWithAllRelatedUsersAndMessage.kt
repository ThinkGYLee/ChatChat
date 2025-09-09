package com.gyleedev.domain.model

data class ChatRoomDataWithAllRelatedUsersAndMessage(
    val chatRoomAndReceiverLocalData: ChatRoomAndReceiverLocalData,
    val receiversInfo: List<RelatedUserLocalData>,
    val lastMessageData: MessageData,
)
// view에서 lastMessage 매핑하는 용
