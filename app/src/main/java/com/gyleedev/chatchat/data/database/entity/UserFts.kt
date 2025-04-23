package com.gyleedev.chatchat.data.database.entity

import androidx.room.Entity
import androidx.room.Fts4

@Fts4(contentEntity = UserEntity::class)
@Entity(tableName = "user_fts")
class UserFts(
    val id: Long,
    val name: String
)
