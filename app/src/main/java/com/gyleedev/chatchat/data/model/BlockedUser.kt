package com.gyleedev.chatchat.data.model

import com.gyleedev.chatchat.domain.RelatedUserLocalData

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
