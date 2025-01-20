package com.gyleedev.chatchat.ui.chatlist

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.gyleedev.chatchat.R
import com.gyleedev.chatchat.domain.ChatRoomDataWithFriendAndMessage
import com.skydoves.landscapist.glide.GlideImage
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChatListViewModel = hiltViewModel()
) {
    val chatRoomList = viewModel.chatRoomList.collectAsLazyPagingItems()
    LaunchedEffect(Unit) {
        viewModel.fetchState.collect {
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "채팅방")
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(imageVector = Icons.Outlined.Add, contentDescription = "add friend")
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        if (chatRoomList.itemCount > 0) {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
            ) {
                items(
                    chatRoomList.itemCount,
                    key = { chatRoomList[it]!!.chatRoomLocalData.id },
                    contentType = { 0 }
                ) { index ->
                    chatRoomList[index]?.let { ChatRoomItem(onClick = onClick, it) }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun ChatRoomItem(
    onClick: (String) -> Unit,
    chatRoomDataWithFriendAndMessage: ChatRoomDataWithFriendAndMessage,
    modifier: Modifier = Modifier
) {
    Row(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .clickable {
                onClick(chatRoomDataWithFriendAndMessage.friendData.uid)
            },
        horizontalArrangement = Arrangement.Absolute.SpaceBetween
    ) {
        Row(Modifier) {
            GlideImage(imageModel = { chatRoomDataWithFriendAndMessage.friendData.picture.ifBlank { R.drawable.icons8__ } })
            Spacer(modifier = Modifier.width(20.dp))
            Column {
                Text(
                    text = chatRoomDataWithFriendAndMessage.friendData.name,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = chatRoomDataWithFriendAndMessage.lastMessageData.comment)
            }
        }

        Text(
            text = LocalDate.ofInstant(
                Instant.ofEpochMilli(chatRoomDataWithFriendAndMessage.lastMessageData.time),
                ZoneId.of("Asia/Seoul")
            ).format(
                DateTimeFormatter.ofPattern("MM-dd")
            )
        )
    }
}
