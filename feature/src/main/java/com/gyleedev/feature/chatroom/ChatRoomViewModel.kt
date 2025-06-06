package com.gyleedev.feature.chatroom

import android.os.Build
import android.text.SpannableString
import android.text.util.Linkify
import android.text.util.Linkify.WEB_URLS
import androidx.annotation.RequiresApi
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.gyleedev.core.BaseViewModel
import com.gyleedev.domain.model.ChatRoomLocalData
import com.gyleedev.domain.model.MessageData
import com.gyleedev.domain.model.MessageSendState
import com.gyleedev.domain.model.MessageType
import com.gyleedev.domain.model.RelatedUserLocalData
import com.gyleedev.domain.model.UserRelationState
import com.gyleedev.domain.usecase.BlockRelatedUserUseCase
import com.gyleedev.domain.usecase.CancelMessageUseCase
import com.gyleedev.domain.usecase.DeleteMessageUseCase
import com.gyleedev.domain.usecase.GetChatRoomDataUseCase
import com.gyleedev.domain.usecase.GetChatRoomLocalDataByUidUseCase
import com.gyleedev.domain.usecase.GetFriendDataUseCase
import com.gyleedev.domain.usecase.GetMessagesFromLocalUseCase
import com.gyleedev.domain.usecase.GetMessagesFromRemoteUseCase
import com.gyleedev.domain.usecase.GetMyUidFromLogInDataUseCase
import com.gyleedev.domain.usecase.ResendMessageUseCase
import com.gyleedev.domain.usecase.SendMessageUseCase
import com.gyleedev.domain.usecase.UserToFriendUseCase
import com.gyleedev.util.FirebaseServerTimeHelper
import com.gyleedev.util.NetworkManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ChatRoomViewModel @Inject constructor(
    getMyUidFromLogInDataUseCase: GetMyUidFromLogInDataUseCase,
    private val getFriendDataUseCase: GetFriendDataUseCase,
    private val getChatRoomLocalDataByUidUseCase: GetChatRoomLocalDataByUidUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val getMessagesFromLocalUseCase: GetMessagesFromLocalUseCase,
    private val getChatRoomDataUseCase: GetChatRoomDataUseCase,
    private val getMessagesFromRemoteUseCase: GetMessagesFromRemoteUseCase,
    private val getNetworkState: NetworkManager,
    private val resendMessageUseCase: ResendMessageUseCase,
    private val cancelMessageUseCase: CancelMessageUseCase,
    private val userToFriendUseCase: UserToFriendUseCase,
    private val blockRelatedUserUseCase: BlockRelatedUserUseCase,
    private val deleteMessageUseCase: DeleteMessageUseCase,
    private val firebaseServerTimeHelper: FirebaseServerTimeHelper,
    savedStateHandle: SavedStateHandle
) : BaseViewModel() {
    private val relatedUserLocalData = MutableStateFlow(RelatedUserLocalData())
    private var uid: String? = null
    private val myUid = getMyUidFromLogInDataUseCase().onEach { uid = it }

    private val _messageQuery = MutableStateFlow("")
    private val _chatRoomLocalData = MutableStateFlow(ChatRoomLocalData())

    private val _photoUri = MutableStateFlow("")
    val photoUri: StateFlow<String> = _photoUri

    private val _networkState = MutableSharedFlow<Boolean>()
    val networkState: SharedFlow<Boolean> = _networkState

    private val _replyTarget =
        MutableStateFlow<SelectedMessageState>(SelectedMessageState.NotSelected)
    val replyTarget: StateFlow<SelectedMessageState> = _replyTarget

    @OptIn(ExperimentalCoroutinesApi::class)
    val messages = _chatRoomLocalData.flatMapLatest {
        getMessagesFromLocalUseCase(it.rid).cachedIn(viewModelScope)
    }

    val uiState = combine(relatedUserLocalData, myUid) { friendData, uid ->
        if (uid != null) {
            ChatRoomUiState.Success(
                userName = friendData.name,
                uid = uid,
                relationState = friendData.userRelation
            )
        } else {
            ChatRoomUiState.Loading
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        ChatRoomUiState.Loading
    )

    private val messagesCallback = relatedUserLocalData.flatMapLatest {
        getMessagesFromRemoteUseCase(_chatRoomLocalData.value, it.userRelation)
    }

    init {
        val passedFriendUid = savedStateHandle.get<String>("friend")
        if (passedFriendUid == null) {
            // TODO 화면 종료 처리
            throw Exception("예외 처리 에러 말고 단거로")
        }
        viewModelScope.launch {
            val friend = getFriendDataUseCase(passedFriendUid).first()
            relatedUserLocalData.emit(friend)
            getChatRoomDataUseCase(friend)
            getChatRoomFromLocal(friend)
            messagesCallback.collectLatest { }
        }
    }

    private suspend fun getChatRoomFromLocal(relatedUserLocalData: RelatedUserLocalData) {
        val data = getChatRoomLocalDataByUidUseCase(relatedUserLocalData.uid)
        _chatRoomLocalData.emit(data)
    }

    fun editMessageQuery(query: String) {
        viewModelScope.launch {
            _messageQuery.emit(query)
        }
    }

    fun editPhotoUri(uri: String) {
        viewModelScope.launch {
            _photoUri.emit(uri)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun sendPhotoMessage() {
        viewModelScope.launch {
            val photoUrl = photoUri.value
            val networkState = getNetworkState()
            editPhotoUri("")
            _networkState.emit(networkState)
            val message = uid?.let {
                MessageData(
                    chatRoomId = _chatRoomLocalData.value.rid,
                    writer = it,
                    type = MessageType.Photo,
                    comment = photoUrl,
                    time = firebaseServerTimeHelper.getEstimatedServerTime(),
                    messageSendState = MessageSendState.LOADING
                )
            }
            val rid = _chatRoomLocalData.value.id
            if (message != null) {
                sendMessageUseCase(message, rid, networkState)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun sendMessage(
        selectedMessage: SelectedMessageState = SelectedMessageState.NotSelected
    ) {
        viewModelScope.launch {
            val networkState = getNetworkState()
            _networkState.emit(networkState)

            var replyTo: String? = null
            var replyComment: String? = null
            var replyKey: Long? = null
            var replyType: MessageType? = null
            if (selectedMessage is SelectedMessageState.Reply) {
                replyTo = selectedMessage.messageData.writer
                replyComment = selectedMessage.messageData.comment
                replyType = selectedMessage.messageData.type
                replyKey = selectedMessage.messageData.time
            }

            val type = isCommentContainLink(_messageQuery.value)
            val comment = if (type == MessageType.Link) {
                isLinkStartFromHttp(_messageQuery.value)
            } else {
                _messageQuery.value
            }

            val message = uid?.let {
                MessageData(
                    chatRoomId = _chatRoomLocalData.value.rid,
                    writer = it,
                    type = type,
                    comment = comment,
                    time = Instant.now().toEpochMilli(),
                    messageSendState = MessageSendState.LOADING,
                    replyKey = replyKey,
                    replyTo = replyTo,
                    replyComment = replyComment,
                    replyType = replyType
                )
            }
            val rid = _chatRoomLocalData.value.id
            if (message != null) {
                sendMessageUseCase(message, rid, networkState)
            }
        }
    }

    private fun getNetworkState(): Boolean {
        return getNetworkState.checkNetworkState()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun resendMessage(messageData: MessageData) {
        viewModelScope.launch {
            val networkState = getNetworkState()
            _networkState.emit(networkState)
            val rid = _chatRoomLocalData.value.id
            resendMessageUseCase(
                messageData,
                rid,
                networkState
            )
        }
    }

    fun cancelMessage(messageData: MessageData) {
        viewModelScope.launch {
            cancelMessageUseCase(messageData)
        }
    }

    private fun isCommentContainLink(comment: String): MessageType {
        val spannableString = SpannableString.valueOf(comment)
        return if (Linkify.addLinks(spannableString, WEB_URLS)) {
            MessageType.Link
        } else {
            MessageType.Text
        }
    }

    private fun isLinkStartFromHttp(query: String): String {
        return if (query.startsWith("http") || query.startsWith("https")) {
            query
        } else {
            "http://$query"
        }
    }

    fun blockUser() {
        viewModelScope.launch {
            blockRelatedUserUseCase(relatedUserLocalData.value)
            val changedData = relatedUserLocalData.value.copy(
                userRelation = UserRelationState.BLOCKED,
                favoriteState = false
            )
            relatedUserLocalData.emit(changedData)
        }
    }

    fun userToFriend() {
        viewModelScope.launch {
            userToFriendUseCase(relatedUserLocalData.value)
            val changedData = relatedUserLocalData.value.copy(
                userRelation = UserRelationState.FRIEND
            )
            relatedUserLocalData.emit(changedData)
        }
    }

    fun deleteMessage(messageData: MessageData) {
        viewModelScope.launch {
            deleteMessageUseCase(messageData).first()
        }
    }

    fun changeReplyTarget(selectedMessage: SelectedMessageState) {
        viewModelScope.launch {
            _replyTarget.emit(selectedMessage)
        }
    }
}
