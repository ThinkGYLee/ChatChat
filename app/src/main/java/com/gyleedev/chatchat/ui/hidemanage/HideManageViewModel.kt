package com.gyleedev.chatchat.ui.hidemanage

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.gyleedev.chatchat.core.BaseViewModel
import com.gyleedev.chatchat.domain.RelatedUserLocalData
import com.gyleedev.chatchat.domain.usecase.GetHideFriendsUseCase
import com.gyleedev.chatchat.domain.usecase.GetHideFriendsWithNameUseCase
import com.gyleedev.chatchat.domain.usecase.UserToFriendUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HideManageViewModel @Inject constructor(
    private val getHideFriendsUseCase: GetHideFriendsUseCase,
    private val userToFriendUseCase: UserToFriendUseCase,
    private val getHideFriendsWithNameUseCase: GetHideFriendsWithNameUseCase
) : BaseViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: MutableStateFlow<String> = _searchQuery

    private val _searchFailure = MutableSharedFlow<Unit>()
    val searchFailure: SharedFlow<Unit> = _searchFailure

    val items = getHideFriendsUseCase().cachedIn(viewModelScope)

    fun editSearchQuery(query: String) {
        viewModelScope.launch {
            _searchQuery.emit(query)
        }
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val searchItems = searchQuery.debounce(500).flatMapLatest {
        getHideFriendsWithNameUseCase(it)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        PagingData.empty()
    )

    fun userToFriend(friend: RelatedUserLocalData) {
        viewModelScope.launch {
            userToFriendUseCase(friend)
        }
    }
}
