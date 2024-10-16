package com.gyleedev.chatchat.ui.chatroom

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.gyleedev.chatchat.domain.FriendData
import com.gyleedev.chatchat.ui.theme.ChatChatTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatRoomScreen(
    friendData: FriendData,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = "${friendData.name} 님과의 대화") },
                navigationIcon = {
                    IconButton(
                        onClick = { /*TODO*/ }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "arrowback button"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {

        }
    }
}

@Preview
@Composable
fun ChatRoomScreenPreview() {
    ChatChatTheme {
        ChatRoomScreen(FriendData(uid = "aa", id = 0L, name = "abcd", picture = " ", status = " "))
    }
}