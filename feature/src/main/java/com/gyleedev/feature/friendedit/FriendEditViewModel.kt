package com.gyleedev.feature.friendedit

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.gyleedev.core.BaseViewModel
import com.gyleedev.domain.model.RelatedUserLocalData
import com.gyleedev.domain.usecase.GetFriendsUseCase
import com.gyleedev.domain.usecase.GetFriendsWithNameUseCase
import com.gyleedev.domain.usecase.HideFriendUseCase
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
class FriendEditViewModel @Inject constructor(
    getFriendsUseCase: GetFriendsUseCase,
    private val getFriendsWithNameUseCase: GetFriendsWithNameUseCase,
    private val hideFriendUseCase: HideFriendUseCase
) : BaseViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: MutableStateFlow<String> = _searchQuery

    private val _searchFailure = MutableSharedFlow<Unit>()
    val searchFailure: SharedFlow<Unit> = _searchFailure

    val items = getFriendsUseCase().cachedIn(viewModelScope)

    fun editSearchQuery(query: String) {
        viewModelScope.launch {
            _searchQuery.emit(query)
        }
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val searchItems = searchQuery
        .debounce(500)
        .flatMapLatest {
            getFriendsWithNameUseCase(it)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            PagingData.empty()
        )

    fun hideFriend(friend: RelatedUserLocalData) {
        viewModelScope.launch {
            hideFriendUseCase(friend)
        }
    }
}
