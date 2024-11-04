package com.gyleedev.chatchat.ui.chatroom

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.gyleedev.chatchat.core.BaseViewModel
import com.gyleedev.chatchat.domain.ChatRoomData
import com.gyleedev.chatchat.domain.ChatRoomLocalData
import com.gyleedev.chatchat.domain.FriendData
import com.gyleedev.chatchat.domain.MessageData
import com.gyleedev.chatchat.domain.MessageSendState
import com.gyleedev.chatchat.domain.UserChatRoomData
import com.gyleedev.chatchat.domain.usecase.CheckChatRoomExistsUseCase
import com.gyleedev.chatchat.domain.usecase.CreateChatRoomUseCase
import com.gyleedev.chatchat.domain.usecase.CreateFriendChatRoomUseCase
import com.gyleedev.chatchat.domain.usecase.CreateLocalChatRoomUseCase
import com.gyleedev.chatchat.domain.usecase.CreateMyChatRoomUseCase
import com.gyleedev.chatchat.domain.usecase.GetChatRoomLocalDataByUidUseCase
import com.gyleedev.chatchat.domain.usecase.GetFriendDataUseCase
import com.gyleedev.chatchat.domain.usecase.GetMessagesFromLocalUseCase
import com.gyleedev.chatchat.domain.usecase.GetMyUidFromLogInDataUseCase
import com.gyleedev.chatchat.domain.usecase.SendMessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class ChatRoomViewModel @Inject constructor(
    private val getMyUidFromLogInDataUseCase: GetMyUidFromLogInDataUseCase,
    private val getFriendDataUseCase: GetFriendDataUseCase,
    private val checkChatRoomExistsUseCase: CheckChatRoomExistsUseCase,
    private val createChatRoomUseCase: CreateChatRoomUseCase,
    private val createMyChatRoomUseCase: CreateMyChatRoomUseCase,
    private val createFriendChatRoomUseCase: CreateFriendChatRoomUseCase,
    private val createLocalChatRoomUseCase: CreateLocalChatRoomUseCase,
    private val getChatRoomLocalDataByUidUseCase: GetChatRoomLocalDataByUidUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val getMessagesFromLocalUseCase: GetMessagesFromLocalUseCase,
    savedStateHandle: SavedStateHandle
) : BaseViewModel() {
    private val _dummyData = MutableStateFlow(dummyUserChatRoomData)
    val dummyData: StateFlow<UserChatRoomData> = _dummyData

    private val _dummyMessageData = MutableStateFlow(dummyMessageDataList)
    val dummyMessageData: StateFlow<List<MessageData>> = _dummyMessageData

    private val _friendData = MutableStateFlow(FriendData())
    val friendData: StateFlow<FriendData> = _friendData

    private val _myUid = MutableStateFlow<String?>(null)
    val myUid: StateFlow<String?> = _myUid

    private val _messageQuery = MutableStateFlow("")

    private val _chatRoomRemoteData = MutableStateFlow<ChatRoomData?>(null)
    private val _chatRoomLocalData = MutableStateFlow<ChatRoomLocalData>(ChatRoomLocalData())

    @OptIn(ExperimentalCoroutinesApi::class)
    val messages = _chatRoomLocalData.flatMapLatest {
        getMessagesFromLocalUseCase(it.rid).cachedIn(viewModelScope)
    }

    init {
        val friend = savedStateHandle.get<String>("friend")
        viewModelScope.launch {
            val uid = getMyUidFromLogInDataUseCase.invoke()
            _myUid.emit(uid)
            if (friend != null) {
                getFriendData(friend)
            }
        }
    }

    private suspend fun getFriendData(friend: String) {
        val friendData = getFriendDataUseCase(friend)
        _friendData.emit(friendData)
        checkChatRoomData()
    }

    private suspend fun checkChatRoomData() {
        val chatRoomStatus = checkChatRoomExistsUseCase(_friendData.value)
        chatRoomStatus.collect {
            if (it) {
                try {
                    getChatRoomFromLocal()
                } catch (e: Exception) {
                    // TODO
                }
            } else {
                createChatRoom()
            }
        }
    }

    private suspend fun createChatRoom() {
        val response = createChatRoomUseCase()
        response.collect {
            if (it != null) {
                _chatRoomRemoteData.emit(it)
                createLocalChatRoom()
                createMyChatRoom()
            } else {
                createChatRoom()
            }
        }
    }

    private suspend fun createMyChatRoom() {
        val response = _chatRoomRemoteData.value?.let {
            createMyChatRoomUseCase(_friendData.value, it)
        }
        response?.collect {
            if (it == null) {
                createMyChatRoom()
            }
            createFriendChatRoom()
        }
    }

    private suspend fun createFriendChatRoom() {
        val response =
            _chatRoomRemoteData.value?.let { createFriendChatRoomUseCase(_friendData.value, it) }
        response?.collect {
            if (it == null) {
                createFriendChatRoom()
            } else {
                getChatRoomFromLocal()
            }
        }
    }

    private suspend fun createLocalChatRoom() {
        if (_chatRoomRemoteData.value != null) {
            createLocalChatRoomUseCase(
                _chatRoomRemoteData.value!!.rid,
                friendData.value.uid
            ).also { println(it) }
        }
    }

    private suspend fun getChatRoomFromLocal() {
        val data = getChatRoomLocalDataByUidUseCase(_friendData.value.uid).also { println(it) }
        _chatRoomLocalData.emit(data)
    }

    fun editMessageQuery(query: String) {
        viewModelScope.launch {
            _messageQuery.emit(query)
        }
    }

    fun sendMessage() {
        viewModelScope.launch {
            val message = myUid.value?.let {
                MessageData(
                    chatRoomId = _chatRoomLocalData.value.rid,
                    writer = it,
                    comment = _messageQuery.value,
                    time = Instant.now().toEpochMilli(),
                    messageSendState = MessageSendState.LOADING
                )
            }
            val rid = _chatRoomLocalData.value.id
            if (message != null) {
                sendMessageUseCase(message, rid)
            }
        }
    }

    fun resendMessage() {
        viewModelScope.launch {
            val message = myUid.value?.let {
                MessageData(
                    chatRoomId = _chatRoomLocalData.value.rid,
                    writer = it,
                    comment = _messageQuery.value,
                    time = Instant.now().toEpochMilli(),
                    messageSendState = MessageSendState.LOADING
                )
            }
            val rid = _chatRoomLocalData.value.id
            if (message != null) {
                sendMessageUseCase(message, rid)
            }
        }
    }
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
    MessageData(
        "AAKKSDKDK",
        "user2",
        "저야말로 잘 부탁드립니다. 저야말로 잘 부탁드립니다. 저야말로 잘 부탁드립니다. 저야말로 잘 부탁드립니다. 저야말로 잘 부탁드립니다.저야말로 잘 부탁드립니다. 저야말로 잘 부탁드립니다. 저야말로 잘 부탁드립니다. 저야말로 잘 부탁드립니다.",
        8L
    )
)
