package com.gyleedev.chatchat.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gyleedev.chatchat.domain.RelatedUserLocalData
import com.gyleedev.chatchat.domain.UserData
import com.gyleedev.chatchat.domain.UserRelationState

@Entity(
    tableName = "user"
)
data class UserEntity(
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
    val status: String,
    @ColumnInfo(name = "relation")
    val relation: UserRelationState,
    @ColumnInfo(name = "favoriteState")
    val favoriteState: Boolean
)

fun RelatedUserLocalData.toEntity(): UserEntity {
    return UserEntity(
        id = id,
        name = name,
        email = email,
        uid = uid,
        picture = picture,
        status = status,
        relation = userRelation,
        favoriteState = favoriteState
    )
}

fun UserEntity.toRelationLocalData(): RelatedUserLocalData {
    return RelatedUserLocalData(
        id = id,
        name = name,
        email = email,
        uid = uid,
        picture = picture,
        status = status,
        userRelation = relation,
        favoriteState = favoriteState
    )
}

fun UserEntity.toModel(): UserData {
    return UserData(
        name = name,
        email = email,
        uid = uid,
        picture = picture,
        status = status
    )
}

fun UserData.toEntityAsFriend(): UserEntity {
    return UserEntity(
        id = 0L,
        name = name,
        email = email,
        uid = uid,
        picture = picture,
        status = status,
        relation = UserRelationState.FRIEND,
        favoriteState = false
    )
}
