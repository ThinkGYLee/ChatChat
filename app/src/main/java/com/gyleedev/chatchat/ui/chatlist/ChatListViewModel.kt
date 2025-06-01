package com.gyleedev.chatchat.ui.chatlist

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.gyleedev.chatchat.domain.model.ChatRoomDataWithAllRelatedUsersAndMessage
import com.gyleedev.chatchat.domain.model.MessageData
import com.gyleedev.chatchat.domain.model.MessageSendState
import com.gyleedev.chatchat.domain.model.MessageType
import com.gyleedev.chatchat.domain.usecase.GetChatRoomDataWithRelatedUserUseCase
import com.gyleedev.chatchat.domain.usecase.GetLastMessageUseCase
import com.gyleedev.core.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatListViewModel @Inject constructor(
    getChatRoomDataWithRelatedUserUseCase: GetChatRoomDataWithRelatedUserUseCase,
    getLastMessageUseCase: GetLastMessageUseCase
) : BaseViewModel() {

    private val _chatRoomList =
        MutableStateFlow<PagingData<ChatRoomDataWithAllRelatedUsersAndMessage>>(PagingData.empty())
    val chatRoomList: StateFlow<PagingData<ChatRoomDataWithAllRelatedUsersAndMessage>> = _chatRoomList

    init {
        viewModelScope.launch {
            getChatRoomDataWithRelatedUserUseCase().cachedIn(viewModelScope).collectLatest { chatRoomAndRelatedUsers ->
                _chatRoomList.emit(
                    chatRoomAndRelatedUsers.map {
                        ChatRoomDataWithAllRelatedUsersAndMessage(
                            chatRoomLocalData = it.chatRoomLocalData,
                            relatedUserLocalData = it.relatedUserLocalData,
                            lastMessageData = getLastMessageUseCase(it.chatRoomLocalData)
                                ?: MessageData("", "", "", type = MessageType.Text, 0L, MessageSendState.COMPLETE)
                        )
                    }
                )
            }
        }
    }
}
