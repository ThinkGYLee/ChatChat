package com.gyleedev.chatchat.domain

import com.google.gson.annotations.SerializedName
import com.gyleedev.chatchat.data.model.RelatedUserRemoteData

// 기본값 설정 안해주면 crash남
data class RelatedUserLocalData(
    @SerializedName("id") val id: Long = 0,
    @SerializedName("email") val email: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("uid") val uid: String = "",
    @SerializedName("picture") val picture: String = "",
    @SerializedName("status") val status: String = "",
    @SerializedName("userRelation") val userRelation: UserRelationState = UserRelationState.UNKNOWN,
    @SerializedName("favoriteState") val favoriteState: Boolean = false,
    @SerializedName("favoriteNumber") val favoriteNumber: Long? = null
)

fun RelatedUserLocalData.toRemoteData(): RelatedUserRemoteData {
    return RelatedUserRemoteData(
        email = email,
        uid = uid,
        status = status,
        name = name,
        picture = picture,
        userRelation = userRelation,
        favoriteState = favoriteState
    )
}
