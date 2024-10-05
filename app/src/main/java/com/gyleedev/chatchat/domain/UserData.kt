package com.gyleedev.chatchat.domain

import com.google.gson.annotations.SerializedName

data class UserData(
    @SerializedName("email") val email: String,
    @SerializedName("name") val name: String,
    @SerializedName("uid") val uid: String
)
