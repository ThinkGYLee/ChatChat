package com.gyleedev.domain.model

sealed interface GetChatRoomState {

    // Common
    data object None : GetChatRoomState
    data object CheckAndGetDataFromLocal : GetChatRoomState
    data object CreatingRemoteChatRoomData : GetChatRoomState
    data object SavingGetChatRoomToLocal : GetChatRoomState
    data object ReturnChatRoom : GetChatRoomState

    // ByUid
    data object CheckingRemoteGetChatRoomExists : GetChatRoomState
    data object GetRemoteData : GetChatRoomState
    data object CheckingMyDataExists : GetChatRoomState
    data object CheckingFriendDataExists : GetChatRoomState
    data object CheckingMyReceiverExists : GetChatRoomState
    data object CheckingFriendReceiverExists : GetChatRoomState
    data object InsertMyDataToReceiver : GetChatRoomState
    data object InsertFriendDataToReceiver : GetChatRoomState
    data object UpdateMyData : GetChatRoomState
    data object UpdateFriendData : GetChatRoomState

    // ByRid
    data object StartFromGetRemoteAndInsertToLocal : GetChatRoomState
    data object GetAndSyncReceivers : GetChatRoomState
    data object InsertReceiversToLocal : GetChatRoomState

    // CreateGroupChat
    data object CreateRemoteGroupChatRoom : GetChatRoomState
    data object InsertUserChatRoomsToRemote : GetChatRoomState
    data object InsertReceiversToRemote : GetChatRoomState

    data class Success(
        val data: ChatRoomAndReceiverLocalData,
    ) : GetChatRoomState
}
