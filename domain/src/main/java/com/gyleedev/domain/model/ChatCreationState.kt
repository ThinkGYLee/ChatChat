package com.gyleedev.domain.model

sealed interface ChatCreationState {
    data object CheckingLocal : ChatCreationState
    data object CheckingRemote : ChatCreationState
    data object CreatingRemoteChatRoom : ChatCreationState
    data object UpdatingChatRoomToMyRemote : ChatCreationState
    data object UpdatingChatRoomToFriendRemote : ChatCreationState
    data object SavingToLocal : ChatCreationState
    data class Success(
        val data: ChatRoomLocalData
    ) : ChatCreationState

    data class Failure(
        val failurePoint: ChatCreationState
    ) : ChatCreationState
}
