package com.gyleedev.chatchat.ui.friendinfo

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
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gyleedev.chatchat.R
import com.skydoves.landscapist.components.rememberImageComponent
import com.skydoves.landscapist.glide.GlideImage
import com.skydoves.landscapist.placeholder.shimmer.Shimmer
import com.skydoves.landscapist.placeholder.shimmer.ShimmerPlugin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendInfoScreen(
    onCloseKeyPressed: () -> Unit,
    onChatRoomClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FriendInfoViewModel = hiltViewModel()
) {
    val friendData by viewModel.friendData.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onCloseKeyPressed) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "close button"
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
                .fillMaxSize(),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GlideImage(
                imageModel = {
                    friendData?.picture?.ifBlank { R.drawable.icons8__ }
                },
                modifier = Modifier
                    .sizeIn(
                        80.dp
                    )
                    .clip(RoundedCornerShape(20.dp)),
                component = rememberImageComponent {
                    +ShimmerPlugin(
                        Shimmer.Flash(
                            baseColor = Color.White,
                            highlightColor = Color.LightGray
                        )
                    )
                },
                previewPlaceholder = painterResource(id = R.drawable.icons8__)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(text = friendData?.name ?: "anonymous")
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = friendData?.status ?: "")
            Spacer(modifier = Modifier.height(60.dp))
            Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Column(
                    modifier = Modifier.clickable {
                        friendData?.let { onChatRoomClick(it.uid) }
                    },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(imageVector = Icons.Outlined.Email, contentDescription = "message button")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "채팅하기")
                }
            }
            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}
