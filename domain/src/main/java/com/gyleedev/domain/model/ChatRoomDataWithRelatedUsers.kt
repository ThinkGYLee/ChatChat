package com.gyleedev.domain.model

data class ChatRoomDataWithRelatedUsers(
    val chatRoomLocalData: ChatRoomLocalData,
    val relatedUserLocalData: RelatedUserLocalData
)
// 레포지토리에서 맵핑하는 용
