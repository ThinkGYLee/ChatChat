package com.gyleedev.chatchat.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

// 코스 정보
@Entity(
    tableName = "favorite",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("user_entity_id"),
            onDelete = ForeignKey.CASCADE
        )
    ]

)
data class FavoriteEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,
    @ColumnInfo(name = "user_entity_id")
    val userEntityId: Long,
    @ColumnInfo(name = "favorite_state")
    val favoriteState: Boolean,
    @ColumnInfo(name = "favorite_number")
    val favoriteNumber: Long? = null,
)
