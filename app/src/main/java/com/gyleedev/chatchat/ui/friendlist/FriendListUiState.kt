package com.gyleedev.chatchat.ui.friendlist

import com.gyleedev.chatchat.domain.RelatedUserLocalData
import com.gyleedev.chatchat.domain.UserData

sealed interface FriendListUiState {

    data object Loading : FriendListUiState

    data class MyData(
        val myData: UserData?
    ) : FriendListUiState

    data class Title(
        val text: String
    ) : FriendListUiState

    data class FavoriteData(
        val favoriteData: RelatedUserLocalData
    ) : FriendListUiState

    data class FriendData(
        val friendData: RelatedUserLocalData
    ) : FriendListUiState
}
