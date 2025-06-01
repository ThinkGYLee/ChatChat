package com.gyleedev.chatchat.domain.model

data class BlockedUser(
    val uid: String = "",
    val email: String = ""
)

fun RelatedUserLocalData.toBlockedUser(): BlockedUser {
    return BlockedUser(
        uid = uid,
        email = email
    )
}
