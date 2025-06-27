package com.gyleedev.data.database.entity

import androidx.room.Embedded
import androidx.room.Relation
import com.gyleedev.domain.model.ChatRoomAndReceiverLocalData

// 코스 정보
data class ChatRoomAndReceiverEntity(
    @Embedded val chatroom: ChatRoomEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "chatroom_entity_id"
    )
    val receivers: List<ReceiverEntity>
)

fun ChatRoomAndReceiverEntity.toLocalData(): ChatRoomAndReceiverLocalData {
    return ChatRoomAndReceiverLocalData(
        id = chatroom.id,
        rid = chatroom.rid,
        lastMessage = chatroom.lastMessage,
        isGroup = chatroom.isGroup,
        receivers = receivers.map { it.receiver }
    )
}
