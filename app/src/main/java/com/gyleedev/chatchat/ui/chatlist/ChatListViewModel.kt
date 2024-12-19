package com.gyleedev.chatchat.ui.chatlist

import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.gyleedev.chatchat.core.BaseViewModel
import com.gyleedev.chatchat.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val repository: UserRepository
) : BaseViewModel() {

    val list = repository.getChatRoomListFromLocal().cachedIn(viewModelScope)

}
