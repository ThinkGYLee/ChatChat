package com.gyleedev.chatchat.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gyleedev.chatchat.domain.MessageData
import com.gyleedev.chatchat.domain.MessageSendState
import com.gyleedev.chatchat.domain.MessageType

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
    @ColumnInfo(name = "type")
    val type: MessageType,
    @ColumnInfo(name = "writer")
    val writer: String,
    @ColumnInfo(name = "comment")
    val comment: String,
    @ColumnInfo(name = "time")
    val time: Long,
    @ColumnInfo(name = "messageSendState")
    val messageSendState: MessageSendState,
    @ColumnInfo(name = "replyTo")
    val replyTo: String?,
    @ColumnInfo(name = "replyComment")
    val replyComment: String?,
    @ColumnInfo(name = "replyType")
    val replyType: MessageType?,
    @ColumnInfo(name = "replyKey")
    val replyKey: Long?,
)

fun MessageEntity.toModel(): MessageData {
    return MessageData(
        chatRoomId = rid,
        writer = writer,
        comment = comment,
        time = time,
        type = type,
        messageSendState = messageSendState,
        replyTo = replyTo,
        replyKey = replyKey,
        replyType = replyType,
        replyComment = replyComment
    )
}

fun MessageData.toEntity(roomId: Long): MessageEntity {
    return MessageEntity(
        id = 0,
        rid = chatRoomId,
        writer = writer,
        comment = comment,
        time = time,
        type = type,
        roomId = roomId,
        messageSendState = messageSendState,
        replyTo = replyTo,
        replyKey = replyKey,
        replyType = replyType,
        replyComment = replyComment
    )
}

fun MessageData.toUpdateEntity(messageId: Long, roomId: Long): MessageEntity {
    return MessageEntity(
        id = messageId,
        rid = chatRoomId,
        writer = writer,
        comment = comment,
        type = type,
        time = time,
        roomId = roomId,
        messageSendState = messageSendState,
        replyTo = replyTo,
        replyKey = replyKey,
        replyType = replyType,
        replyComment = replyComment
    )
}
