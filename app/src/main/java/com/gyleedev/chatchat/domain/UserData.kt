package com.gyleedev.chatchat.domain

import com.google.gson.annotations.SerializedName
import com.gyleedev.chatchat.data.model.BlockedUser

// User정보
// 기본값 설정 안해주면 crash남
data class UserData(
    @SerializedName("email") val email: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("uid") val uid: String = "",
    @SerializedName("picture") val picture: String = "",
    @SerializedName("status") val status: String = ""
)

fun UserData.toBlockedUser(): BlockedUser {
    return BlockedUser(
        uid = uid,
        email = email
    )
}