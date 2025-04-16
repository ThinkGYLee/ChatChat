package com.gyleedev.chatchat.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gyleedev.chatchat.domain.RelatedUserLocalData
import com.gyleedev.chatchat.domain.UserData

@Entity(
    tableName = "friend"
)
data class FriendEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long,
    @ColumnInfo(name = "email")
    val email: String,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "uid")
    val uid: String,
    @ColumnInfo(name = "picture")
    val picture: String,
    @ColumnInfo(name = "status")
    val status: String
)

fun UserData.toEntity(): FriendEntity {
    return FriendEntity(
        id = 0,
        name = name,
        email = email,
        uid = uid,
        picture = picture,
        status = status
    )
}

fun FriendEntity.toFriendData(): RelatedUserLocalData {
    return RelatedUserLocalData(
        id = id,
        name = name,
        email = email,
        uid = uid,
        picture = picture,
        status = status
    )
}

fun FriendEntity.toModel(): UserData {
    return UserData(
        name = name,
        email = email,
        uid = uid,
        picture = picture,
        status = status
    )
}
