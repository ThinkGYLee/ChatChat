package com.gyleedev.chatchat.ui.chatroom

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.gyleedev.chatchat.ui.theme.ChatChatTheme

@Composable
fun ChatRoomScreen(modifier: Modifier = Modifier) {
    Scaffold(modifier = modifier) { innerPadding ->
        Column(modifier = Modifier
            .padding(innerPadding)
            .consumeWindowInsets(innerPadding)) {

        }
    }
}

@Preview
@Composable
fun ChatRoomScreenPreview() {
    ChatChatTheme {
        ChatRoomScreen()
    }
}