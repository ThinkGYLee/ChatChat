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
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
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

    private val friendData = MutableStateFlow(FriendData())

    private var uid: String? = null
    private val myUid = getMyUidFromLogInDataUseCase()
        .onEach { uid = it }

    private val _messageQuery = MutableStateFlow("")
    private val _chatRoomLocalData = MutableStateFlow(ChatRoomLocalData())

    @OptIn(ExperimentalCoroutinesApi::class)
    val messages = _chatRoomLocalData.flatMapLatest {
        getMessagesFromLocalUseCase(it.rid).cachedIn(viewModelScope)
    }

    val uiState = combine(friendData, myUid) { friendData, uid ->
        if (uid != null) {
            ChatRoomUiState.Success(
                userName = friendData.name,
                uid = uid
            )
        } else {
            ChatRoomUiState.Loading
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        ChatRoomUiState.Loading
    )

    private var job: Job? = null

    init {
        val friendUid = savedStateHandle.get<String>("friend")
        if (friendUid == null) {
            // TODO 화면 종료 처리
            throw Exception("예외 처리 에러 말고 단거로")
        }
        viewModelScope.launch {
            val friend = getFriendDataUseCase(friendUid).first()
            friendData.emit(friend)
            getChatRoomDataUseCase(friend)
            getChatRoomFromLocal()

            getMessagesFromRemoteUseCase(_chatRoomLocalData.value).collectLatest { }
            // connectRemote()
        }
    }

    fun connectRemote() {
        if (_chatRoomLocalData.value.rid.isEmpty()) {
            return
        }
        job = viewModelScope.launch {
            getMessagesFromRemoteUseCase(_chatRoomLocalData.value).collectLatest { }
        }
    }

    fun disconnectRemote() {
        job?.cancel()
    }

    private suspend fun getChatRoomFromLocal() {
        val data = getChatRoomLocalDataByUidUseCase(friendData.value.uid)
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
            val message = uid?.let {
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
