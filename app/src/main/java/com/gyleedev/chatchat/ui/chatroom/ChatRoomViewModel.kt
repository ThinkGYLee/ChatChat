package com.gyleedev.chatchat.ui.chatroom

import com.gyleedev.chatchat.core.BaseViewModel
import com.gyleedev.chatchat.domain.MessageData
import com.gyleedev.chatchat.domain.UserChatRoomData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class ChatRoomViewModel @Inject constructor() : BaseViewModel() {
    private val _dummyData = MutableStateFlow(dummyUserChatRoomData)
    val dummyData: StateFlow<UserChatRoomData> = _dummyData

    private val _dummyMessageData = MutableStateFlow(dummyMessageDataList)
    val dummyMessageData: StateFlow<List<MessageData>> = _dummyMessageData
}

val dummyUserChatRoomData = UserChatRoomData(
    "AAKKSDKDK",
    "user1"
)

val dummyMessageDataList = listOf(
    MessageData("AAKKSDKDK", "user1", "안녕하세요! 반갑습니다.", 0L),
    MessageData("AAKKSDKDK", "user2", "안녕하세요", 1L),
    MessageData("AAKKSDKDK", "user2", "자기소개 부탁드립니다!", 2L),
    MessageData("AAKKSDKDK", "user1", "저는 프로그래머 지망생입니다.!", 3L),
    MessageData("AAKKSDKDK", "user1", "지금 안드로이드를 공부하고 있습니다.!", 4L),
    MessageData("AAKKSDKDK", "user2", "공부하신지 얼마나 되셨나요?", 5L),
    MessageData("AAKKSDKDK", "user1", "이제 한달 다 되어갑니다. 잘 부탁드립니다.", 6L),
    MessageData("AAKKSDKDK", "user2", "저야말로 잘 부탁드립니다.", 7L),
    MessageData("AAKKSDKDK", "user2", "저야말로 잘 부탁드립니다. 저야말로 잘 부탁드립니다. 저야말로 잘 부탁드립니다. 저야말로 잘 부탁드립니다. 저야말로 잘 부탁드립니다.저야말로 잘 부탁드립니다. 저야말로 잘 부탁드립니다. 저야말로 잘 부탁드립니다. 저야말로 잘 부탁드립니다.", 8L)
)
