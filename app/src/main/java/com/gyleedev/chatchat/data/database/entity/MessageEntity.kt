package com.gyleedev.chatchat.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gyleedev.chatchat.domain.MessageData

@Entity(
    tableName = "message"
)

/*
 rid는 realtimeDatabase의 id
 roomId는 roomDatabase의 id
*/
data class MessageEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long,
    @ColumnInfo(name = "roomId")
    val roomId: Long,
    @ColumnInfo(name = "rid")
    val rid: String,
    @ColumnInfo(name = "writer")
    val writer: String,
    @ColumnInfo(name = "comment")
    val comment: String,
    @ColumnInfo(name = "time")
    val time: Long
)

fun MessageEntity.toModel(): MessageData {
    return MessageData(
        chatRoomId = rid,
        writer = writer,
        comment = comment,
        time = time
    )
}

fun MessageData.toEntity(roomId: Long): MessageEntity {
    return MessageEntity(
        id = 0,
        rid = chatRoomId,
        writer = writer,
        comment = comment,
        time = time,
        roomId = roomId
    )
}
