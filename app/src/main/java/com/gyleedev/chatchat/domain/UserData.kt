package com.gyleedev.chatchat.domain

import com.google.gson.annotations.SerializedName

// 기본값 설정 안해주면 crash남
data class UserData(
    @SerializedName("email") val email: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("uid") val uid: String = "",
    @SerializedName("picture") val picture: String = "",
    @SerializedName("status") val status: String = ""
)
