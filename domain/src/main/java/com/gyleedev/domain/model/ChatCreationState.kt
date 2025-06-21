package com.gyleedev.domain.model

sealed interface ChatCreationState {
    data object CheckingLocal : ChatCreationState
    data object CheckingRemoteChatRoomExists : ChatCreationState
    data object CheckingMyDataExists : ChatCreationState
    data object CheckingFriendDataExists : ChatCreationState
    data object CreatingRemoteChatRoom : ChatCreationState
    data object UpdateMyData : ChatCreationState
    data object UpdateFriendData : ChatCreationState
    data object SavingChatRoomToLocal : ChatCreationState
    data object GetDataFromLocal : ChatCreationState
    data class Success(
        val data: ChatRoomLocalData
    ) : ChatCreationState

    data class Failure(
        val failurePoint: ChatCreationState
    ) : ChatCreationState
}

/*
    check local
    check remote roomdata exists
    check remote mydata
    check remote frienddata
    create room
    update myData
    update friendData
    insert data to local
    get data from local

 */