package com.gyleedev.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gyleedev.domain.model.ChatRoomLocalData

@Entity(
    tableName = "chatroom"
)
data class ChatRoomEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long,
    @ColumnInfo(name = "rid")
    val rid: String,
    @ColumnInfo(name = "lastMessage")
    val lastMessage: String,
    @ColumnInfo(name = "isGroup")
    val isGroup: Boolean
)

fun ChatRoomEntity.toModel(): ChatRoomLocalData {
    return ChatRoomLocalData(
        id = id,
        rid = rid,
        lastMessage = lastMessage,
        isGroup = isGroup
    )
}
