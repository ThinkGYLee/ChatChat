package com.gyleedev.feature.chatlist

import com.gyleedev.core.BaseViewModel
import com.gyleedev.domain.usecase.GetChatRoomDataWithRelatedUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ChatListViewModel @Inject constructor(
    getChatRoomDataWithRelatedUserUseCase: GetChatRoomDataWithRelatedUserUseCase
) : BaseViewModel() {
    val uiState = getChatRoomDataWithRelatedUserUseCase()
}
