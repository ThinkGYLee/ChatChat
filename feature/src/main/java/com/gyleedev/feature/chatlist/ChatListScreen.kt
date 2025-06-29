package com.gyleedev.feature.chatlist

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.paging.compose.collectAsLazyPagingItems
import com.gyleedev.domain.model.ChatRoomDataWithAllRelatedUsersAndMessage
import com.gyleedev.domain.model.MessageType
import com.gyleedev.feature.R
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.components.rememberImageComponent
import com.skydoves.landscapist.glide.GlideImage
import com.skydoves.landscapist.placeholder.shimmer.Shimmer
import com.skydoves.landscapist.placeholder.shimmer.ShimmerPlugin
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChatListViewModel = hiltViewModel()
) {
    val chatRoomList = viewModel.uiState.collectAsLazyPagingItems()
    LaunchedEffect(Unit) {
        viewModel.fetchState.collect {
        }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        chatRoomList.refresh()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.chat_list_screen_title))
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
                    key = { requireNotNull(chatRoomList[it]).chatRoomAndReceiverLocalData.id },
                    contentType = { 0 }
                ) { index ->
                    ChatRoomItem(
                        onClick = onClick,
                        requireNotNull(chatRoomList[index])
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun ChatRoomItem(
    onClick: (String) -> Unit,
    chatRoomDataWithAllRelatedUsersAndMessage: ChatRoomDataWithAllRelatedUsersAndMessage,
    modifier: Modifier = Modifier
) {
    val text = when (chatRoomDataWithAllRelatedUsersAndMessage.lastMessageData.type) {
        MessageType.Text -> chatRoomDataWithAllRelatedUsersAndMessage.lastMessageData.comment
        MessageType.Photo -> stringResource(R.string.chat_list_screen_chat_type_photo)
        MessageType.Link -> chatRoomDataWithAllRelatedUsersAndMessage.lastMessageData.comment
        else -> ""
    }

    Row(
        modifier
            .fillMaxWidth()
            .clickable {
                onClick(chatRoomDataWithAllRelatedUsersAndMessage.chatRoomAndReceiverLocalData.rid)
            }
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.Absolute.SpaceBetween
    ) {
        Row(Modifier, verticalAlignment = Alignment.CenterVertically) {
            GlideImage(
                imageModel = {
                    chatRoomDataWithAllRelatedUsersAndMessage.receiversInfo[0].picture.ifBlank { R.drawable.baseline_person_24 }
                },
                imageOptions = ImageOptions(contentScale = ContentScale.Crop),
                modifier = Modifier
                    .size(48.dp)
                    .border(
                        width = 0.01.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = CircleShape
                    )
                    .clip(CircleShape)
                    .background(color = colorResource(R.color.avatar_background)),
                component = rememberImageComponent {
                    +ShimmerPlugin(
                        Shimmer.Flash(
                            baseColor = Color.White,
                            highlightColor = Color.LightGray
                        )
                    )
                },
                previewPlaceholder = painterResource(id = R.drawable.baseline_person_24)
            )
            Spacer(modifier = Modifier.width(20.dp))
            Column(verticalArrangement = Arrangement.Center) {
                Text(
                    text = chatRoomDataWithAllRelatedUsersAndMessage.receiversInfo[0].name,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(2.dp))
                if (chatRoomDataWithAllRelatedUsersAndMessage.receiversInfo[0].status.isNotBlank()) {
                    Text(text = text, style = MaterialTheme.typography.labelMedium, maxLines = 2)
                }
            }
        }

        Text(
            text = LocalDate.ofInstant(
                Instant.ofEpochMilli(chatRoomDataWithAllRelatedUsersAndMessage.lastMessageData.time),
                ZoneId.of("Asia/Seoul")
            ).format(
                DateTimeFormatter.ofPattern("MM-dd")
            ),
            style = MaterialTheme.typography.labelMedium
        )
    }
}
