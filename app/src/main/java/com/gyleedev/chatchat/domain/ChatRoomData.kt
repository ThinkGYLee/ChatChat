package com.gyleedev.chatchat.domain

import com.google.gson.annotations.SerializedName

// 기본값 설정 안해주면 crash남
data class ChatRoomData(
    @SerializedName("id") val rid: String = "",
    @SerializedName("lastMessage") val lastMessage: String = "",
)
//챗룸정보 테이블 -> 챗룸id를 키값으로
//유저별 챗룸 정보 테이블 -> 유저 아이디를 키값으로 사용 내용에 상대Uid, 챗방Uid 저장
