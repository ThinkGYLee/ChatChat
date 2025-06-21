package com.gyleedev.domain.model

sealed interface ChatCreationState {
    data object CheckAndGetDataFromLocal : ChatCreationState
    data object CheckingRemoteChatRoomExists : ChatCreationState
    data object GetRemoteData: ChatCreationState
    data object CheckingMyDataExists : ChatCreationState
    data object CheckingFriendDataExists : ChatCreationState
    data object CreatingRemoteChatRoom : ChatCreationState
    data object UpdateMyData : ChatCreationState
    data object UpdateFriendData : ChatCreationState
    data object SavingChatRoomToLocal : ChatCreationState
    data class Success(
        val data: ChatRoomLocalData
    ) : ChatCreationState

    data class Failure(
        val failurePoint: ChatCreationState
    ) : ChatCreationState
}