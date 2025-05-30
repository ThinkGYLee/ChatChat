package com.gyleedev.chatchat.ui.chatroom

import com.gyleedev.chatchat.domain.MessageData

sealed interface SelectedMessageState {

    data object NotSelected : SelectedMessageState

    data class Reply(
        val messageData: MessageData
    ) : SelectedMessageState

    data class Selected(
        val messageData: MessageData
    ) : SelectedMessageState
}
