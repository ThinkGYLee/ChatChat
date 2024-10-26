package com.gyleedev.chatchat.ui.chatroom

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Send
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gyleedev.chatchat.domain.MessageData
import com.gyleedev.chatchat.ui.theme.ChatChatTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatRoomScreen(
    onBackPressKeyClick: () -> Unit,
    modifier: Modifier = Modifier,
    chatRoomViewModel: ChatRoomViewModel = hiltViewModel()
) {
    val dummyData = chatRoomViewModel.dummyData.collectAsStateWithLifecycle()
    val dummyMessageData = chatRoomViewModel.dummyMessageData.collectAsStateWithLifecycle()
    val me = "user1"
    val friendData = chatRoomViewModel.friendData.collectAsStateWithLifecycle()
    val query = rememberTextFieldState()

    LaunchedEffect(query.text) {
        chatRoomViewModel.editMessageQuery(query.text.toString())
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = "${friendData.value.name} 님과의 대화") },
                navigationIcon = {
                    IconButton(
                        onClick = onBackPressKeyClick
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "arrowback button"
                        )
                    }
                }
            )
        },
        bottomBar = {
            CommentBottomBar(query = query)
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState(), reverseScrolling = true),
        ) {
            dummyMessageData.value.forEach {
                ChatBubble(me, it)
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun ChatBubble(me: String, messageData: MessageData, modifier: Modifier = Modifier) {
    val backgroundColor: Color
    val backgroundShape: RoundedCornerShape
    val alignment: Alignment.Horizontal

    if (messageData.writer == me) {
        backgroundColor = MaterialTheme.colorScheme.primary
        backgroundShape = RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp)
        alignment = Alignment.End
    } else {
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant
        backgroundShape = RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp)
        alignment = Alignment.Start
    }

    Column(
        modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Surface(
            color = backgroundColor,
            shape = backgroundShape
        ) {
            Text(text = messageData.comment, modifier = Modifier.padding(16.dp))
        }
    }
}

@Composable
fun CommentBottomBar(
    query: TextFieldState,
    modifier: Modifier = Modifier
) {

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
                        onClick = { },
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
        }
    )
}


@Preview
@Composable
fun ChatBubblePreview() {
    ChatChatTheme {
        ChatBubble("user1", dummyMessageDataList[1])
    }
}

@Preview
@Composable
fun ChatRoomScreenPreview() {
    ChatChatTheme {
        ChatRoomScreen(onBackPressKeyClick = {})
    }
}
