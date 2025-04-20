package com.gyleedev.chatchat.data.model

import com.google.gson.annotations.SerializedName
import com.gyleedev.chatchat.domain.RelatedUserLocalData
import com.gyleedev.chatchat.domain.UserRelationState

// 기본값 설정 안해주면 crash남
data class RelatedUserRemoteData(
    @SerializedName("email") val email: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("uid") val uid: String = "",
    @SerializedName("picture") val picture: String = "",
    @SerializedName("status") val status: String = "",
    @SerializedName("userRelation") val userRelation: UserRelationState = UserRelationState.UNKNOWN,
    @SerializedName("favorite") val favorite: Boolean = false
)

fun RelatedUserRemoteData.toRelatedUserLocalData(): RelatedUserLocalData {
    return RelatedUserLocalData(
        id = 0L,
        email = email,
        name = name,
        uid = uid,
        picture = picture,
        status = status,
        userRelation = userRelation,
        favorite = favorite
    )
}
