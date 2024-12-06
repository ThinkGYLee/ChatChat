package com.gyleedev.chatchat.ui.chatlist

import com.gyleedev.chatchat.core.BaseViewModel
import com.gyleedev.chatchat.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val repository: UserRepository
) : BaseViewModel() {

}
