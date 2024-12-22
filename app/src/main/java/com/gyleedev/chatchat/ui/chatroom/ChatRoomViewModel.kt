package com.gyleedev.chatchat.ui.chatroom

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.gyleedev.chatchat.core.BaseViewModel
import com.gyleedev.chatchat.domain.ChatRoomLocalData
import com.gyleedev.chatchat.domain.FriendData
import com.gyleedev.chatchat.domain.MessageData
import com.gyleedev.chatchat.domain.MessageSendState
import com.gyleedev.chatchat.domain.usecase.GetChatRoomDataUseCase
import com.gyleedev.chatchat.domain.usecase.GetChatRoomLocalDataByUidUseCase
import com.gyleedev.chatchat.domain.usecase.GetFriendDataUseCase
import com.gyleedev.chatchat.domain.usecase.GetMessagesFromLocalUseCase
import com.gyleedev.chatchat.domain.usecase.GetMessagesFromRemoteUseCase
import com.gyleedev.chatchat.domain.usecase.GetMyUidFromLogInDataUseCase
import com.gyleedev.chatchat.domain.usecase.SendMessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class ChatRoomViewModel @Inject constructor(
    private val getMyUidFromLogInDataUseCase: GetMyUidFromLogInDataUseCase,
    private val getFriendDataUseCase: GetFriendDataUseCase,
    private val getChatRoomLocalDataByUidUseCase: GetChatRoomLocalDataByUidUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val getMessagesFromLocalUseCase: GetMessagesFromLocalUseCase,
    private val getChatRoomDataUseCase: GetChatRoomDataUseCase,
    private val getMessagesFromRemoteUseCase: GetMessagesFromRemoteUseCase,
    savedStateHandle: SavedStateHandle
) : BaseViewModel() {

    private val _friendData = MutableStateFlow(FriendData())
    val friendData: StateFlow<FriendData> = _friendData

    private val _myUid = MutableStateFlow<String?>(null)
    val myUid: StateFlow<String?> = _myUid

    private val _messageQuery = MutableStateFlow("")
    private val _chatRoomLocalData = MutableStateFlow(ChatRoomLocalData())

    @OptIn(ExperimentalCoroutinesApi::class)
    val messages = _chatRoomLocalData.flatMapLatest {
        getMessagesFromLocalUseCase(it.rid).cachedIn(viewModelScope)
    }

    init {
        val friend = savedStateHandle.get<String>("friend")
        viewModelScope.launch {
            val uid = getMyUidFromLogInDataUseCase()
            _myUid.emit(uid)
            if (friend != null) {
                getFriendData(friend)
            }
        }
    }

    private suspend fun getFriendData(friend: String) {
        val friendData = getFriendDataUseCase(friend)
        _friendData.emit(friendData)
        getChatRoomDataUseCase(friendData)
        getChatRoomFromLocal()
        getMessagesFromRemoteUseCase(_chatRoomLocalData.value).collectLatest { }
    }

    private suspend fun getChatRoomFromLocal() {
        val data = getChatRoomLocalDataByUidUseCase(_friendData.value.uid)
        _chatRoomLocalData.emit(data)
    }

    fun editMessageQuery(query: String) {
        viewModelScope.launch {
            _messageQuery.emit(query)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
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
}
