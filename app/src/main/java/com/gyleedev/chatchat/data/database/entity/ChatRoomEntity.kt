package com.gyleedev.chatchat.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gyleedev.chatchat.domain.ChatRoomLocalData

@Entity(
    tableName = "chatroom"
)
data class ChatRoomEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long,
    @ColumnInfo(name = "rid")
    val rid: String,
    @ColumnInfo(name = "receiver")
    val receiver: String,
    @ColumnInfo(name = "lastMessage")
    val lastMessage: String
)

fun ChatRoomEntity.toModel(): ChatRoomLocalData {
    return ChatRoomLocalData(
        id = id,
        rid = rid,
        receiver = receiver,
        lastMessage = lastMessage
    )
}
