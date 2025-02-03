package com.gyleedev.chatchat.ui.chatroom

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.delete
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import com.gyleedev.chatchat.R
import com.gyleedev.chatchat.domain.MessageData
import com.gyleedev.chatchat.domain.MessageSendState
import com.gyleedev.chatchat.ui.theme.ChatChatTheme
import kotlinx.coroutines.flow.collectLatest

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatRoomScreen(
    onBackPressKeyClick: () -> Unit,
    modifier: Modifier = Modifier,
    chatRoomViewModel: ChatRoomViewModel = hiltViewModel()
) {
    val query = rememberTextFieldState()
    val messages = chatRoomViewModel.messages.collectAsLazyPagingItems()

    val lazyListState = remember {
        mutableStateOf(LazyListState(firstVisibleItemScrollOffset = messages.itemCount))
    }

    val uiState by chatRoomViewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current

    LaunchedEffect(query.text) {
        chatRoomViewModel.editMessageQuery(query.text.toString())
    }

    // 보냈을때 내려간다던지로 변경
    LaunchedEffect(messages.itemCount) {
        lazyListState.value.animateScrollToItem(0)
    }

    LaunchedEffect(Unit) {
        chatRoomViewModel.networkState.flowWithLifecycle(lifecycle.lifecycle).collectLatest {
            if (!it) Toast.makeText(context, "네트워크 연결을 확인해주세요", Toast.LENGTH_SHORT).show()
        }
    }

//    LifecycleStartEffect(Unit) {
//        chatRoomViewModel.connectRemote()
//        onStopOrDispose {
//            chatRoomViewModel.disconnectRemote()
//        }
//    }

    Scaffold(
        modifier = modifier,
        topBar = {
            if (uiState is ChatRoomUiState.Success) {
                TopAppBar(
                    title = { Text(text = "${(uiState as ChatRoomUiState.Success).userName} 님과의 대화") },
                    navigationIcon = {
                        IconButton(
                            onClick = onBackPressKeyClick
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.navigation_arrow_back_icon_description)
                            )
                        }
                    }
                )
            }
        },
        bottomBar = {
            CommentBottomBar(
                query = query,
                onClick = {
                    chatRoomViewModel.sendMessage()
                    query.edit {
                        delete(
                            0,
                            query.text.length
                        )
                    }
                }
            )
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
                    key = { messages[it]?.time!! },
                    contentType = { messages[it]?.writer }
                ) {
                    Row {
                        messages[it]?.let { messageData ->
                            ChatBubble(
                                me = (uiState as ChatRoomUiState.Success).uid,
                                messageData = messageData,
                                resend = { chatRoomViewModel.resendMessage(messageData) },
                                cancel = { chatRoomViewModel.cancelMessage(messageData) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(
    resend: () -> Unit,
    cancel: () -> Unit,
    me: String,
    messageData: MessageData,
    modifier: Modifier = Modifier
) {
    val backgroundColor: Color
    val backgroundShape: RoundedCornerShape
    val arrangement: Arrangement.Horizontal

    if (messageData.writer == me) {
        backgroundColor = MaterialTheme.colorScheme.primary
        backgroundShape = RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp)
        arrangement = Arrangement.End
    } else {
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant
        backgroundShape = RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp)
        arrangement = Arrangement.Start
    }
    Row(
        modifier
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .fillMaxWidth(),
        horizontalArrangement = arrangement,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (messageData.messageSendState == MessageSendState.LOADING) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp))
        } else if (messageData.messageSendState == MessageSendState.FAIL) {
            ResendButton(onResendClick = resend, onCancelClick = cancel)
        }
        Column(
            Modifier.padding(horizontal = 16.dp)
        ) {
            Surface(
                color = backgroundColor,
                shape = backgroundShape
            ) {
                Text(text = messageData.comment, modifier = Modifier.padding(16.dp))
            }
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
            .fillMaxWidth(),
        decorator = { innerTextField ->
            Box {
                if (query.text.isEmpty()) {
                    Text(
                        text = "채팅을 입력하세요",
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
                            contentDescription = "Reply Icon",
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
            }
        },
        textStyle = TextStyle.Default.copy(color = color)
    )
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
            modifier = Modifier
                .clickable {
                    onResendClick()
                }
        ) {
            Icon(
                imageVector = Icons.Outlined.Refresh,
                contentDescription = "resend button",
                modifier = Modifier.padding(2.dp)
            )
        }
        Surface(
            color = Color.Red,
            shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp),
            modifier = Modifier
                .clickable {
                    onCancelClick()
                }
        ) {
            Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = "cancel button",
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

@RequiresApi(Build.VERSION_CODES.O)
@Preview
@Composable
fun ChatRoomScreenPreview() {
    ChatChatTheme {
        ChatRoomScreen(onBackPressKeyClick = {})
    }
}
