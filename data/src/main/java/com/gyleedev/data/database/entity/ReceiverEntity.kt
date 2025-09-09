package com.gyleedev.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.gyleedev.domain.model.UserChatRoomReceiver

@Entity(
    tableName = "receiver",
    foreignKeys = [
        ForeignKey(
            entity = ChatRoomEntity::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("chatroom_entity_id"),
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["chatroom_entity_id"])],
)
data class ReceiverEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,
    @ColumnInfo(name = "chatroom_entity_id")
    val chatRoomEntityId: Long,
    @ColumnInfo(name = "receiver")
    val receiver: String,
)

fun UserChatRoomReceiver.toEntity(chatRoomEntityId: Long): ReceiverEntity = ReceiverEntity(
    id = 0L,
    chatRoomEntityId = chatRoomEntityId,
    receiver = receiver,
)
