package com.gyleedev.chatchat.ui.chatroom

import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.delete
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.PersonAddAlt
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.RemoveCircleOutline
import androidx.compose.material.icons.outlined.ReportProblem
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import com.gyleedev.chatchat.R
import com.gyleedev.chatchat.domain.MessageData
import com.gyleedev.chatchat.domain.MessageSendState
import com.gyleedev.chatchat.domain.MessageType
import com.gyleedev.chatchat.domain.UrlMetaData
import com.gyleedev.chatchat.domain.UserRelationState
import com.gyleedev.chatchat.ui.theme.ChatChatTheme
import com.gyleedev.chatchat.util.getImageFromFireStore
import com.gyleedev.chatchat.util.getMedaData
import com.skydoves.landscapist.components.rememberImageComponent
import com.skydoves.landscapist.glide.GlideImage
import com.skydoves.landscapist.placeholder.shimmer.Shimmer
import com.skydoves.landscapist.placeholder.shimmer.ShimmerPlugin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ChatRoomScreen(
    onBackPressKeyClick: () -> Unit,
    modifier: Modifier = Modifier,
    chatRoomViewModel: ChatRoomViewModel = hiltViewModel()
) {
    val query = rememberTextFieldState()
    val messages = chatRoomViewModel.messages.collectAsLazyPagingItems()
    val photoUri = chatRoomViewModel.photoUri.collectAsStateWithLifecycle()
    var openMessageDialog by remember { mutableStateOf(false) }
    var openDeleteDialog by remember { mutableStateOf(false) }
    val replyTarget = chatRoomViewModel.replyTarget.collectAsStateWithLifecycle()

    val lazyListState = remember {
        mutableStateOf(
            LazyListState(firstVisibleItemScrollOffset = messages.itemCount)
        )
    }

    val uiState by chatRoomViewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current

    val screenWidth = LocalWindowInfo.current.containerSize.width

    val pickMedia =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            // Callback is invoked after the user selects a media item or closes the
            // photo picker.
            if (uri != null) {
                chatRoomViewModel.editPhotoUri(uri.toString())
            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }

    LaunchedEffect(query.text) {
        chatRoomViewModel.editMessageQuery(query.text.toString())
    }

    // TODO 보냈을때 내려간다던지로 변경
    LaunchedEffect(messages.itemCount) {
        lazyListState.value.animateScrollToItem(0)
    }

    LaunchedEffect(Unit) {
        chatRoomViewModel.networkState.flowWithLifecycle(lifecycle.lifecycle).collectLatest {
            if (!it) {
                Toast.makeText(context, R.string.network_error_message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            if (uiState is ChatRoomUiState.Success) {
                val uiStateCast = uiState as ChatRoomUiState.Success
                ChatRoomTopBar(
                    onUnblockClick = chatRoomViewModel::userToFriend,
                    onBlockClick = chatRoomViewModel::blockUser,
                    onAddFriendClick = chatRoomViewModel::userToFriend,
                    onBackPressKeyClick = onBackPressKeyClick,
                    state = uiStateCast.relationState,
                    name = uiStateCast.userName
                )
            }
        },
        bottomBar = {
            if (uiState is ChatRoomUiState.Success) {
                if ((uiState as ChatRoomUiState.Success).relationState == UserRelationState.BLOCKED) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(text = stringResource(R.string.chat_room_blocked_user_bottom_bar_text))
                    }
                } else {
                    UniversalBar(
                        onPhotoButtonClick = {
                            pickMedia.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        query = query,
                        onTextSendButtonClick = {
                            chatRoomViewModel.sendMessage(selectedMessage = chatRoomViewModel.replyTarget.value)
                            query.edit { delete(0, query.text.length) }
                            chatRoomViewModel.changeReplyTarget(SelectedMessageState.NotSelected)
                        },
                        onPhotoSendButtonClick = chatRoomViewModel::sendPhotoMessage,
                        onCancelClick = { chatRoomViewModel.editPhotoUri("") },
                        selectedMessageData = replyTarget.value,
                        replyEnd = { chatRoomViewModel.changeReplyTarget(SelectedMessageState.NotSelected) },
                        screenWidth = screenWidth,
                        photoUri = photoUri.value,
                        uiState = uiState as ChatRoomUiState.Success
                    )
                }
            }
        }
    ) { innerPadding ->
        if (uiState is ChatRoomUiState.Success) {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
                    .fillMaxSize(),
                state = lazyListState.value,
                reverseLayout = true
            ) {
                items(
                    count = messages.itemCount,
                    key = { requireNotNull(messages[it]?.time) },
                    contentType = { messages[it]?.writer }
                ) {
                    Row {
                        messages[it]?.let { messageData ->
                            when (messageData.type) {
                                MessageType.Text -> {
                                    ChatBubble(
                                        me = (uiState as ChatRoomUiState.Success).uid,
                                        replyTo = (uiState as ChatRoomUiState.Success).userName,
                                        messageData = messageData,
                                        resend = { chatRoomViewModel.resendMessage(messageData) },
                                        cancel = { chatRoomViewModel.cancelMessage(messageData) },
                                        onLongClick = {
                                            chatRoomViewModel.changeReplyTarget(
                                                SelectedMessageState.Selected(messageData)
                                            )
                                            openMessageDialog = true
                                        }
                                    )
                                }

                                MessageType.Photo -> {
                                    PhotoBubble(
                                        me = (uiState as ChatRoomUiState.Success).uid,
                                        messageData = messageData,
                                        resend = { chatRoomViewModel.resendMessage(messageData) },
                                        cancel = { chatRoomViewModel.cancelMessage(messageData) }
                                    )
                                }

                                MessageType.Link -> {
                                    LinkBubble(
                                        me = (uiState as ChatRoomUiState.Success).uid,
                                        messageData = messageData,
                                        resend = { chatRoomViewModel.resendMessage(messageData) },
                                        cancel = { chatRoomViewModel.cancelMessage(messageData) }
                                    )
                                }

                                else -> {}
                            }
                        }
                    }
                }
            }
        }

        if (openMessageDialog) {
            MessageDialog(
                closeDialog = {
                    openMessageDialog = false
                },
                onCopy = { },
                onPartialCopy = { },
                onReply = {
                    val messageData =
                        (replyTarget.value as SelectedMessageState.Selected).messageData
                    chatRoomViewModel.changeReplyTarget(SelectedMessageState.Reply(messageData = messageData))
                },
                openDeleteDialog = {
                    openDeleteDialog = true
                },
                resetDialogData = { chatRoomViewModel.changeReplyTarget(SelectedMessageState.NotSelected) }
            )
        }

        if (openDeleteDialog) {
            val messageData = (replyTarget.value as SelectedMessageState.Selected).messageData
            DeleteDialog(
                isMessageMine = messageData.writer == (uiState as ChatRoomUiState.Success).uid,
                closeDialog = { openDeleteDialog = false },
                onDelete = {
                    chatRoomViewModel.deleteMessage(messageData)
                    chatRoomViewModel.changeReplyTarget(SelectedMessageState.NotSelected)
                }
            )
        }
    }
}

@Composable
fun ChatBubbleDivider(
    width: Int?,
    modifier: Modifier = Modifier,
    thickness: Dp = DividerDefaults.Thickness
) {
    val density = LocalDensity.current
    val color: Color = if (isSystemInDarkTheme()) {
        MaterialTheme.colorScheme.outline
    } else {
        DividerDefaults.color
    }

    Canvas(
        modifier
            .then(
                if (width != null) {
                    modifier.width(with(density) { width.toDp() })
                } else {
                    modifier.width(0.dp)
                }
            )
            .height(thickness)
    ) {
        drawLine(
            color = color,
            strokeWidth = thickness.toPx(),
            start = Offset(0f, thickness.toPx() / 2),
            end = Offset(size.width, thickness.toPx() / 2)
        )
    }
}

@Composable
fun ChatBubble(
    resend: () -> Unit,
    cancel: () -> Unit,
    onLongClick: () -> Unit,
    me: String,
    replyTo: String,
    messageData: MessageData,
    modifier: Modifier = Modifier
) {
    val backgroundColor: Color
    val backgroundShape: RoundedCornerShape
    val arrangement: Arrangement.Horizontal
    val rowPaddingModifier: Modifier

    if (messageData.writer == me) {
        backgroundColor = MaterialTheme.colorScheme.primary
        backgroundShape = RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp)
        arrangement = Arrangement.End
        rowPaddingModifier = modifier.padding(start = 80.dp)
    } else {
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant
        backgroundShape = RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp)
        arrangement = Arrangement.Start
        rowPaddingModifier = modifier.padding(end = 80.dp)
    }

    val name = if (messageData.replyTo == me) {
        "나"
    } else {
        replyTo
    }

    var dividerWidth: Int? by remember { mutableStateOf(null) }

    Row(
        rowPaddingModifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .fillMaxWidth(),
        horizontalArrangement = arrangement,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (messageData.messageSendState == MessageSendState.LOADING) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp))
        } else if (messageData.messageSendState == MessageSendState.FAIL) {
            ResendButton(onResendClick = resend, onCancelClick = cancel)
        }
        Surface(
            color = backgroundColor,
            shape = backgroundShape,
            modifier = Modifier
                .combinedClickable(
                    onLongClick = onLongClick,
                    onClick = {}
                )

        ) {
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .onGloballyPositioned { layoutCoordinates ->
                        dividerWidth = layoutCoordinates.size.width
                    }
            ) {
                if (messageData.replyTo != null) {
                    Text(
                        text = "${name}에게 답장",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = requireNotNull(messageData.replyComment),
                        maxLines = 1,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Light
                    )

                    ChatBubbleDivider(
                        width = dividerWidth,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
                Text(text = messageData.comment, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun LinkBubble(
    resend: () -> Unit,
    cancel: () -> Unit,
    me: String,
    messageData: MessageData,
    modifier: Modifier = Modifier
) {
    val backgroundColor: Color
    val backgroundShape: RoundedCornerShape
    val arrangement: Arrangement.Horizontal

    val coroutineScope = rememberCoroutineScope()

    if (messageData.writer == me) {
        backgroundColor = MaterialTheme.colorScheme.primary
        backgroundShape = RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp)
        arrangement = Arrangement.End
    } else {
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant
        backgroundShape = RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp)
        arrangement = Arrangement.Start
    }

    val metaData = remember {
        mutableStateOf(UrlMetaData())
    }
    LaunchedEffect(Unit) {
        coroutineScope.launch(Dispatchers.Default) {
            metaData.value = getMedaData(messageData.comment)
        }
    }

    Row(
        modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth(),
        horizontalArrangement = arrangement,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (messageData.messageSendState == MessageSendState.LOADING) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp))
        } else if (messageData.messageSendState == MessageSendState.FAIL) {
            ResendButton(onResendClick = resend, onCancelClick = cancel)
        }
        Surface(
            color = backgroundColor,
            shape = backgroundShape
        ) {
            Text(text = messageData.comment, modifier = Modifier.padding(16.dp))
        }
    }
}

@Composable
fun PhotoBubble(
    resend: () -> Unit,
    cancel: () -> Unit,
    me: String,
    messageData: MessageData,
    modifier: Modifier = Modifier
) {
    var imageUrl by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(messageData) { imageUrl = getImageFromFireStore(messageData.comment).first() }

    val arrangement: Arrangement.Horizontal = if (messageData.writer == me) {
        Arrangement.End
    } else {
        Arrangement.Start
    }
    Row(
        modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth(),
        horizontalArrangement = arrangement,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (messageData.messageSendState == MessageSendState.LOADING) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp))
        } else if (messageData.messageSendState == MessageSendState.FAIL) {
            ResendButton(onResendClick = resend, onCancelClick = cancel)
        }
        GlideImage(
            imageModel = {
                // place holder size
                imageUrl // .ifBlank { R.drawable.icons8__ }
            },
            modifier = Modifier
                .sizeIn(
                    maxWidth = 200.dp,
                    maxHeight = 320.dp
                )
                .clip(RoundedCornerShape(20.dp)),
            component = rememberImageComponent {
                +ShimmerPlugin(
                    Shimmer.Flash(
                        baseColor = Color.White,
                        highlightColor = Color.LightGray
                    )
                )
            }
        )
    }
}

@Preview
@Composable
fun PhotoBubblePreview() {
    ChatChatTheme {
        PhotoBubble(
            resend = {},
            cancel = {},
            me = "me",
            messageData = MessageData(writer = "", type = MessageType.Photo, comment = "")
        )
    }
}

@Composable
fun PhotoBottomBar(
    screenWidth: Int,
    onSendButtonClick: () -> Unit,
    onCancelButtonClick: () -> Unit,
    uri: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Absolute.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onCancelButtonClick) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = stringResource(R.string.close_button_description)
                )
            }
            Text(
                text = stringResource(R.string.chat_room_photo_bar_text),
                fontWeight = FontWeight.SemiBold
            )
            IconButton(onClick = onSendButtonClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = stringResource(R.string.send_button_description)
                )
            }
        }
        HorizontalDivider()
        Spacer(Modifier.height(8.dp))
        GlideImage(
            imageModel = { uri.toUri() },
            modifier = Modifier
                .padding(12.dp)
                .sizeIn(
                    maxWidth = screenWidth.dp,
                    maxHeight = screenWidth.dp
                )
                .clip(RoundedCornerShape(20.dp)),
            component = rememberImageComponent {
                +ShimmerPlugin(
                    Shimmer.Flash(
                        baseColor = Color.White,
                        highlightColor = Color.LightGray
                    )
                )
            }
        )
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
fun UniversalBar(
    onPhotoButtonClick: () -> Unit,
    onTextSendButtonClick: () -> Unit,
    onPhotoSendButtonClick: () -> Unit,
    onCancelClick: () -> Unit,
    replyEnd: () -> Unit,
    uiState: ChatRoomUiState.Success,
    screenWidth: Int,
    selectedMessageData: SelectedMessageState,
    query: TextFieldState,
    photoUri: String,
    modifier: Modifier = Modifier
) {
    if (photoUri.isEmpty()) {
        Column(modifier = modifier) {
            if (selectedMessageData is SelectedMessageState.Reply) {
                val name = if (selectedMessageData.messageData.writer == uiState.uid) {
                    "나"
                } else {
                    uiState.userName
                }
                ReplyBar(
                    selectedMessageData = selectedMessageData.messageData,
                    name = name,
                    replyEnd = replyEnd
                )
            } else {
                MediaBar(onPhotoButtonClick = onPhotoButtonClick)
            }
            CommentBottomBar(
                onClick = onTextSendButtonClick,
                query = query
            )
        }
    } else {
        PhotoBottomBar(
            onCancelButtonClick = onCancelClick,
            onSendButtonClick = onPhotoSendButtonClick,
            uri = photoUri,
            screenWidth = screenWidth
        )
    }
}

@Composable
fun MediaBar(
    onPhotoButtonClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier.padding(start = 8.dp)) {
        IconButton(onClick = onPhotoButtonClick) {
            Icon(
                imageVector = Icons.Filled.Image,
                contentDescription = stringResource(R.string.media_bar_photo_icon_description)
            )
        }
    }
}

@Composable
fun ReplyBar(
    name: String,
    selectedMessageData: MessageData,
    replyEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier
            .fillMaxWidth()
            .padding(start = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "${name}에게 답장",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = selectedMessageData.comment,
                maxLines = 1,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Light
            )
        }
        IconButton(onClick = replyEnd) {
            Icon(imageVector = Icons.Outlined.Close, contentDescription = null)
        }
    }
}

@Composable
fun CommentBottomBar(
    onClick: () -> Unit,
    query: TextFieldState,
    modifier: Modifier = Modifier
) {
    val color = if (isSystemInDarkTheme()) Color.White else Color.Black
    BasicTextField(
        state = query,
        modifier = modifier
            .fillMaxWidth()
            .imePadding(),
        decorator = { innerTextField ->
            Box {
                if (query.text.isEmpty()) {
                    Text(
                        text = stringResource(R.string.chat_room_screen_chat_hint),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF848484),
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 20.dp)
                    )
                }
                Row(
                    modifier = Modifier
                        .padding(start = 20.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    innerTextField()
                    IconButton(
                        onClick = onClick,
                        enabled = query.text.isNotEmpty()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Send,
                            contentDescription = stringResource(R.string.reply_icon_description),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
            }
        },
        textStyle = TextStyle.Default.copy(color = color)
    )
}

@Preview
@Composable
fun CommentBarPreview() {
    ChatChatTheme {
        CommentBottomBar(onClick = {}, query = TextFieldState())
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatRoomTopBar(
    onUnblockClick: () -> Unit,
    onBlockClick: () -> Unit,
    onAddFriendClick: () -> Unit,
    onBackPressKeyClick: () -> Unit,
    state: UserRelationState,
    name: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(
                        R.string.chat_room_screen_title,
                        name
                    )
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackPressKeyClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.navigation_arrow_back_icon_description)
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        when (state) {
            UserRelationState.BLOCKED -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .sizeIn(minWidth = 80.dp, minHeight = 80.dp)
                            .clip(CircleShape)
                            .clickable { onUnblockClick() },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.RemoveCircleOutline,
                            contentDescription = stringResource(R.string.unblock_icon_description),
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = stringResource(R.string.unblock_icon_text),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }

                    Column(
                        modifier = Modifier
                            .sizeIn(minWidth = 80.dp, minHeight = 80.dp)
                            .clip(CircleShape)
                            .clickable {},
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ReportProblem,
                            contentDescription = stringResource(R.string.report_icon_description),
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = stringResource(R.string.report_icon_text),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }

            UserRelationState.ME -> {
            }

            UserRelationState.UNKNOWN -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = TopAppBarDefaults.topAppBarColors().containerColor)
                ) {
                    Row {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(R.string.chat_room_not_friend_text_up),
                                style = MaterialTheme.typography.labelMedium
                            )
                            Text(
                                text = stringResource(R.string.chat_room_not_friend_text_down),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .sizeIn(minWidth = 80.dp, minHeight = 80.dp)
                                .clip(CircleShape)
                                .clickable { onAddFriendClick() },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.PersonAddAlt,
                                contentDescription = stringResource(R.string.add_friend_icon_description),
                                modifier = Modifier.size(40.dp)
                            )
                            Text(
                                text = stringResource(R.string.add_friend_icon_text),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }

                        Column(
                            modifier = Modifier
                                .sizeIn(minWidth = 80.dp, minHeight = 80.dp)
                                .clip(CircleShape)
                                .clickable { onBlockClick() },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Block,
                                contentDescription = stringResource(R.string.block_icon_description),
                                modifier = Modifier.size(40.dp)
                            )
                            Text(
                                text = stringResource(R.string.block_icon_text),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }

                        Column(
                            modifier = Modifier
                                .sizeIn(minWidth = 80.dp, minHeight = 80.dp)
                                .clip(CircleShape)
                                .clickable {},
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ReportProblem,
                                contentDescription = stringResource(R.string.report_icon_description),
                                modifier = Modifier.size(40.dp)
                            )
                            Text(
                                text = stringResource(R.string.report_icon_text),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }

            else -> {}
        }
    }
}

@Preview
@Composable
fun ChatRoomTopBarPreview() {
    MaterialTheme {
        ChatRoomTopBar(
            onBackPressKeyClick = {},
            state = UserRelationState.UNKNOWN,
            name = "abcd",
            onUnblockClick = {},
            onBlockClick = {},
            onAddFriendClick = {}
        )
    }
}

@Composable
fun ResendButton(
    onResendClick: () -> Unit,
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier) {
        Surface(
            color = Color.Red,
            shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp),
            modifier = Modifier.clickable { onResendClick() }
        ) {
            Icon(
                imageVector = Icons.Outlined.Refresh,
                contentDescription = stringResource(R.string.message_resend_icon_description),
                modifier = Modifier.padding(2.dp)
            )
        }
        Surface(
            color = Color.Red,
            shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp),
            modifier = Modifier.clickable { onCancelClick() }
        ) {
            Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = stringResource(R.string.message_cancel_icon_description),
                modifier = Modifier.padding(2.dp)
            )
        }
    }
}

@Preview
@Composable
fun ResendButtonPreview() {
    ChatChatTheme {
        ResendButton(onResendClick = {}, onCancelClick = {})
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageDialog(
    onCopy: () -> Unit,
    onPartialCopy: () -> Unit,
    onReply: () -> Unit,
    openDeleteDialog: () -> Unit,
    resetDialogData: () -> Unit,
    closeDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    BasicAlertDialog(
        onDismissRequest = closeDialog,
        content = {
            Surface(
                modifier = Modifier.wrapContentSize(),
                shape = MaterialTheme.shapes.medium,
                tonalElevation = AlertDialogDefaults.TonalElevation,
                color = AlertDialogDefaults.containerColor
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = stringResource(R.string.chat_room_dialog_copy_text),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onCopy()
                                resetDialogData()
                                closeDialog()
                            }
                            .padding(horizontal = 24.dp, vertical = 8.dp)
                    )

                    Text(
                        text = stringResource(R.string.chat_room_dialog_copy_partial_text),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onPartialCopy()
                                resetDialogData()
                                closeDialog()
                            }
                            .padding(horizontal = 24.dp, vertical = 8.dp)
                    )
                    Text(
                        text = stringResource(R.string.chat_room_dialog_reply_text),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onReply()
                                closeDialog()
                            }
                            .padding(horizontal = 24.dp, vertical = 8.dp)
                    )
                    Text(
                        text = stringResource(R.string.chat_room_dialog_delete_text),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                openDeleteDialog()
                                closeDialog()
                            }
                            .padding(horizontal = 24.dp, vertical = 8.dp)
                    )
                }
            }
        },
        modifier = modifier.wrapContentSize()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteDialog(
    isMessageMine: Boolean,
    closeDialog: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    BasicAlertDialog(
        onDismissRequest = closeDialog,
        content = {
            Surface(
                modifier = Modifier.wrapContentSize(),
                shape = MaterialTheme.shapes.medium,
                tonalElevation = AlertDialogDefaults.TonalElevation,
                color = AlertDialogDefaults.containerColor
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = if (isMessageMine) {
                            stringResource(R.string.delete_message_dialog_my_message_header)
                        } else {
                            stringResource(R.string.delete_message_dialog_other_message_header)
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.delete_message_dialog_text),
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Absolute.Right
                    ) {
                        TextButton(
                            onClick = {
                                closeDialog()
                            }
                        ) {
                            Text(text = stringResource(R.string.dialog_dismiss_button_text))
                        }
                        TextButton(
                            onClick = {
                                onDelete()
                                closeDialog()
                            }
                        ) {
                            Text(text = stringResource(R.string.dialog_delete_button_text))
                        }
                    }
                }
            }
        },
        modifier = modifier.wrapContentSize()
    )
}
