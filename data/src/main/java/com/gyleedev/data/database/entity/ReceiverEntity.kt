package com.gyleedev.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "receiver",
    foreignKeys = [
        ForeignKey(
            entity = ChatRoomEntity::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("chatroom_entity_id"),
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["chatroom_entity_id"])]
)

data class ReceiverEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,
    @ColumnInfo(name = "chatroom_entity_id")
    val userEntityId: Long,
    @ColumnInfo(name = "receiver")
    val receiver: String
)
