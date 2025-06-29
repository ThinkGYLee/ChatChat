package com.gyleedev.domain.model

data class ChatRoomAndReceiverLocalData(
    val id: Long = 0,
    val rid: String = "",
    val lastMessage: String = "",
    val isGroup: Boolean = false,
    val receivers: List<String> = listOf<String>()
)
// 챗룸정보 테이블 -> 챗룸id를 키값으로
// 유저별 챗룸 정보 테이블 -> 유저 아이디를 키값으로 사용 내용에 상대Uid, 챗방Uid 저장
