package com.gyleedev.domain.model

sealed interface GetChatRoomState {
    data object None : GetChatRoomState
    data object CheckAndGetDataFromLocal : GetChatRoomState
    data object CheckingRemoteGetChatRoomExists : GetChatRoomState
    data object GetRemoteData : GetChatRoomState
    data object CheckingMyDataExists : GetChatRoomState
    data object CheckingFriendDataExists : GetChatRoomState
    data object CreatingRemoteChatRoomData : GetChatRoomState
    data object CheckingMyReceiverExists : GetChatRoomState
    data object CheckingFriendReceiverExists : GetChatRoomState
    data object InsertMyDataToReceiver : GetChatRoomState
    data object InsertFriendDataToReceiver : GetChatRoomState
    data object UpdateMyData : GetChatRoomState
    data object UpdateFriendData : GetChatRoomState
    data object SavingGetChatRoomToLocal : GetChatRoomState
    data class Success(
        val data: ChatRoomAndReceiverLocalData
    ) : GetChatRoomState
}
