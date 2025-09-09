package com.gyleedev.domain.model

data class BlockedUser(
    val uid: String = "",
    val email: String = "",
)

fun RelatedUserLocalData.toBlockedUser(): BlockedUser = BlockedUser(
    uid = uid,
    email = email,
)
