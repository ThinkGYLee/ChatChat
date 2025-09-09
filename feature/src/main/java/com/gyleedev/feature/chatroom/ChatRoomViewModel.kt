package com.gyleedev.feature.chatroom

import android.net.Uri
import android.os.Build
import android.text.SpannableString
import android.text.util.Linkify
import android.text.util.Linkify.WEB_URLS
import androidx.annotation.RequiresApi
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.gyleedev.core.BaseViewModel
import com.gyleedev.domain.model.ChatRoomAndReceiverLocalData
import com.gyleedev.domain.model.GetChatRoomException
import com.gyleedev.domain.model.GetChatRoomState
import com.gyleedev.domain.model.MessageData
import com.gyleedev.domain.model.MessageSendState
import com.gyleedev.domain.model.MessageType
import com.gyleedev.domain.model.RelatedUserLocalData
import com.gyleedev.domain.model.UserRelationState
import com.gyleedev.domain.usecase.BlockRelatedUserUseCase
import com.gyleedev.domain.usecase.CancelMessageUseCase
import com.gyleedev.domain.usecase.CreateChatRoomByUidsUseCase
import com.gyleedev.domain.usecase.DeleteMessageUseCase
import com.gyleedev.domain.usecase.GetChatRoomByRidUseCase
import com.gyleedev.domain.usecase.GetChatRoomByUidUseCase
import com.gyleedev.domain.usecase.GetChatRoomDataStateObserveUseCase
import com.gyleedev.domain.usecase.GetFriendDataUseCase
import com.gyleedev.domain.usecase.GetMessagesFromLocalUseCase
import com.gyleedev.domain.usecase.GetMessagesFromRemoteUseCase
import com.gyleedev.domain.usecase.GetMyUidFromLogInDataUseCase
import com.gyleedev.domain.usecase.GetRelatedUserDataOfParticipantsByUidUseCase
import com.gyleedev.domain.usecase.ResendMessageUseCase
import com.gyleedev.domain.usecase.ResetGetChatDataStateUseCase
import com.gyleedev.domain.usecase.SendMessageUseCase
import com.gyleedev.domain.usecase.UserToFriendUseCase
import com.gyleedev.util.FirebaseServerTimeHelper
import com.gyleedev.util.NetworkManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
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
    private val sendMessageUseCase: SendMessageUseCase,
    private val createChatRoomByUidsUseCase: CreateChatRoomByUidsUseCase,
    private val getMessagesFromLocalUseCase: GetMessagesFromLocalUseCase,
    private val getChatRoomDataStateObserveUseCase: GetChatRoomDataStateObserveUseCase,
    private val resetGetChatDataStateUseCase: ResetGetChatDataStateUseCase,
    private val getMessagesFromRemoteUseCase: GetMessagesFromRemoteUseCase,
    private val getNetworkState: NetworkManager,
    private val resendMessageUseCase: ResendMessageUseCase,
    private val cancelMessageUseCase: CancelMessageUseCase,
    private val userToFriendUseCase: UserToFriendUseCase,
    private val blockRelatedUserUseCase: BlockRelatedUserUseCase,
    private val deleteMessageUseCase: DeleteMessageUseCase,
    private val getChatRoomByUidUseCase: GetChatRoomByUidUseCase,
    private val getChatRoomByRidUseCase: GetChatRoomByRidUseCase,
    private val getRelatedUserDataOfParticipantsByUidUseCase: GetRelatedUserDataOfParticipantsByUidUseCase,
    private val firebaseServerTimeHelper: FirebaseServerTimeHelper,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel() {
    private val chatRoomParticipants = MutableStateFlow<List<RelatedUserLocalData>>(emptyList())
    private var uid: String? = null
    private val myUid = getMyUidFromLogInDataUseCase().onEach { uid = it }
    private val rid = MutableStateFlow<String?>(null)

    private val messageQuery = MutableStateFlow("")
    private val chatRoomLocalData = MutableStateFlow(ChatRoomAndReceiverLocalData())

    private val _photoUri = MutableStateFlow("")
    val photoUri: StateFlow<String> = _photoUri

    private val _networkState = MutableSharedFlow<Boolean>()
    val networkState: SharedFlow<Boolean> = _networkState

    private val _replyTarget =
        MutableStateFlow<SelectedMessageState>(SelectedMessageState.NotSelected)
    val replyTarget: StateFlow<SelectedMessageState> = _replyTarget

    private val _getChatRoomEvent = MutableSharedFlow<GetChatRoomException>()
    val getChatRoomEvent: SharedFlow<GetChatRoomException> = _getChatRoomEvent

    private val getChatRoomState = MutableStateFlow<GetChatRoomState>(GetChatRoomState.None)

    @OptIn(ExperimentalCoroutinesApi::class)
    val messages = chatRoomLocalData.flatMapLatest {
        getMessagesFromLocalUseCase(it.rid).cachedIn(viewModelScope)
    }

    val uiState = combine(
        chatRoomParticipants,
        myUid,
        rid,
        getChatRoomState,
    ) { participants, uid, rid, getChatRoomState ->
        if (rid != null && uid != null && participants.isNotEmpty() && getChatRoomState is GetChatRoomState.Success) {
            ChatRoomUiState.Success(
                userName = if (participants.size == 1) participants[0].name else "${participants[0].name} 외 ${participants.size} 명",
                participants = participants,
                uid = requireNotNull(uid),
                relationState = if (participants.size == 1) participants[0].userRelation else UserRelationState.GROUP,
            )
        } else if (rid == null && uid != null && getChatRoomState is GetChatRoomState.Success) {
            ChatRoomUiState.Success(
                userName = if (participants.size == 1) participants[0].name else "${participants[0].name} 외 ${participants.size} 명",
                uid = uid,
                participants = participants,
                relationState = if (participants.size == 1) participants[0].userRelation else UserRelationState.GROUP,
            )
        } else {
            ChatRoomUiState.Loading
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        ChatRoomUiState.Loading,
    )

    private val messagesCallback = chatRoomParticipants.flatMapLatest {
        val relation = if (it.size == 1) it[0].userRelation else UserRelationState.GROUP
        getMessagesFromRemoteUseCase(chatRoomLocalData.value, relation)
    }

    init {
        val passedRid = savedStateHandle.get<String>("rid")
        val passedFriendUid = savedStateHandle.get<String>("uid")
        val passedCreateArray = savedStateHandle.get<String>("create")

        if (passedFriendUid == null && passedRid == null && passedCreateArray == null) {
            // TODO 화면 종료 처리
            throw Exception("예외 처리 에러 말고 단거로")
        }

        if (passedFriendUid != null) {
            startChatRoomWithUid(passedFriendUid)
        }

        if (passedCreateArray != null) {
            val uidList = passedCreateArray.split(",").map { Uri.decode(it) }.orEmpty()
            createChatRoomByUids(uidList)
        }

        if (passedRid != null) {
            viewModelScope.launch {
                rid.emit(passedRid)
                startChatRoomWithRid(passedRid)
            }
        }
    }

    private fun createChatRoomByUids(uidList: List<String>) {
        viewModelScope.launch {
            try {
                val userList = getRelatedUserDataOfParticipantsByUidUseCase(uidList)
                chatRoomParticipants.emit(userList)
                observeCurrentState()
                val getChatRoomData = createChatRoomByUidsUseCase(userList)
                if (getChatRoomData is GetChatRoomState.Success) {
                    chatRoomLocalData.emit(getChatRoomData.data)
                    resetGetChatDataStateUseCase()
                    messagesCallback.collectLatest { }
                }
            } catch (e: GetChatRoomException) {
                _getChatRoomEvent.emit(e)
            }
        }
    }

    fun startChatRoomWithUid(uid: String) {
        viewModelScope.launch {
            try {
                val friend = getFriendDataUseCase(requireNotNull(uid)).first()
                chatRoomParticipants.emit(listOf(friend))
                observeCurrentState()
                val getChatRoomData = getChatRoomByUidUseCase(
                    chatRoomParticipants.value[0],
                    GetChatRoomState.CheckAndGetDataFromLocal,
                )
                if (getChatRoomData is GetChatRoomState.Success) {
                    chatRoomLocalData.emit(getChatRoomData.data)
                    resetGetChatDataStateUseCase()
                    messagesCallback.collectLatest { }
                }
            } catch (e: GetChatRoomException) {
                println(e)
                _getChatRoomEvent.emit(e)
            }
        }
    }

    fun startChatRoomWithRid(rid: String) {
        viewModelScope.launch {
            try {
                observeCurrentState()
                val getChatRoomData = getChatRoomByRidUseCase(rid)
                if (getChatRoomData is GetChatRoomState.Success) {
                    chatRoomParticipants.emit(
                        getRelatedUserDataOfParticipantsByUidUseCase(
                            getChatRoomData.data.receivers,
                        ),
                    )
                    chatRoomLocalData.emit(getChatRoomData.data)
                    resetGetChatDataStateUseCase()
                    messagesCallback.collectLatest { }
                }
            } catch (e: GetChatRoomException) {
                println(e)
                _getChatRoomEvent.emit(e)
            }
        }
    }

    private fun observeCurrentState() {
        viewModelScope.launch {
            val state = getChatRoomDataStateObserveUseCase()
            state.collect { currentState ->
                if (currentState !is GetChatRoomState.None) {
                    getChatRoomState.emit(currentState)
                }
                if (currentState is GetChatRoomState.Success) {
                    this.cancel()
                    return@collect
                }
            }
        }
    }

    fun editMessageQuery(query: String) {
        viewModelScope.launch {
            messageQuery.emit(query)
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
                    chatRoomId = chatRoomLocalData.value.rid,
                    writer = it,
                    type = MessageType.Photo,
                    comment = photoUrl,
                    time = firebaseServerTimeHelper.getEstimatedServerTime(),
                    messageSendState = MessageSendState.LOADING,
                )
            }
            val rid = chatRoomLocalData.value.id
            if (message != null) {
                sendMessageUseCase(message, rid, networkState)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun sendMessage(
        selectedMessage: SelectedMessageState = SelectedMessageState.NotSelected,
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

            val type = isCommentContainLink(messageQuery.value)
            val comment = if (type == MessageType.Link) {
                isLinkStartFromHttp(messageQuery.value)
            } else {
                messageQuery.value
            }

            val message = uid?.let {
                MessageData(
                    chatRoomId = chatRoomLocalData.value.rid,
                    writer = it,
                    type = type,
                    comment = comment,
                    time = Instant.now().toEpochMilli(),
                    messageSendState = MessageSendState.LOADING,
                    replyKey = replyKey,
                    replyTo = replyTo,
                    replyComment = replyComment,
                    replyType = replyType,
                )
            }
            val rid = chatRoomLocalData.value.id
            if (message != null) {
                sendMessageUseCase(message, rid, networkState)
            }
        }
    }

    private fun getNetworkState(): Boolean = getNetworkState.checkNetworkState()

    @RequiresApi(Build.VERSION_CODES.O)
    fun resendMessage(messageData: MessageData) {
        viewModelScope.launch {
            val networkState = getNetworkState()
            _networkState.emit(networkState)
            val rid = chatRoomLocalData.value.id
            resendMessageUseCase(
                messageData,
                rid,
                networkState,
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

    private fun isLinkStartFromHttp(query: String): String = if (query.startsWith("http") || query.startsWith("https")) {
        query
    } else {
        "http://$query"
    }

    fun blockUser() {
        viewModelScope.launch {
            blockRelatedUserUseCase(chatRoomParticipants.value[0])
            val changedData = chatRoomParticipants.value[0].copy(
                userRelation = UserRelationState.BLOCKED,
                favoriteState = false,
            )
            chatRoomParticipants.emit(listOf(changedData))
        }
    }

    fun userToFriend() {
        viewModelScope.launch {
            userToFriendUseCase(chatRoomParticipants.value[0])
            val changedData = chatRoomParticipants.value[0].copy(
                userRelation = UserRelationState.FRIEND,
            )
            chatRoomParticipants.emit(listOf(changedData))
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
