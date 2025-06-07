package com.gyleedev.data.database.entity

import androidx.room.Embedded
import androidx.room.Relation
import com.gyleedev.domain.model.RelatedUserLocalData

// 코스 정보
data class UserAndFavoriteEntity(
    @Embedded val user: UserEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "user_entity_id"
    )
    val favorite: FavoriteEntity
)

fun UserAndFavoriteEntity.toLocalData(): RelatedUserLocalData {
    return RelatedUserLocalData(
        id = user.id,
        email = user.email,
        uid = user.uid,
        picture = user.picture,
        status = user.status,
        favoriteState = favorite.favoriteState,
        favoriteNumber = favorite.favoriteNumber,
        userRelation = user.relation,
        name = user.name,
        verified = user.verified
    )
}
