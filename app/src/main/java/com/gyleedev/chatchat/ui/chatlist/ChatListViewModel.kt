package com.gyleedev.chatchat.ui.chatlist

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.gyleedev.chatchat.core.BaseViewModel
import com.gyleedev.chatchat.domain.ChatRoomDataWithFriendAndMessage
import com.gyleedev.chatchat.domain.MessageData
import com.gyleedev.chatchat.domain.MessageSendState
import com.gyleedev.chatchat.domain.MessageType
import com.gyleedev.chatchat.domain.usecase.GetChatRoomListUseCase
import com.gyleedev.chatchat.domain.usecase.GetLastMessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatListViewModel @Inject constructor(
    getChatRoomListUseCase: GetChatRoomListUseCase,
    getLastMessageUseCase: GetLastMessageUseCase
) : BaseViewModel() {

    private val _chatRoomList =
        MutableStateFlow<PagingData<ChatRoomDataWithFriendAndMessage>>(PagingData.empty())
    val chatRoomList: StateFlow<PagingData<ChatRoomDataWithFriendAndMessage>> = _chatRoomList

    init {
        viewModelScope.launch {
            getChatRoomListUseCase().cachedIn(viewModelScope).collectLatest { chatRoomAndFriend ->
                _chatRoomList.emit(
                    chatRoomAndFriend.map {
                        ChatRoomDataWithFriendAndMessage(
                            chatRoomLocalData = it.chatRoomLocalData,
                            friendData = it.friendData,
                            lastMessageData = getLastMessageUseCase(it.chatRoomLocalData)
                                ?: MessageData("", "", "", type = MessageType.Text, 0L, MessageSendState.COMPLETE)
                        )
                    }
                )
            }
        }
    }
}
