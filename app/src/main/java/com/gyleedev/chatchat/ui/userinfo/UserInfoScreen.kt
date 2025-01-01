package com.gyleedev.chatchat.ui.userinfo

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
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
fun UserInfoScreen(
    onBackPressKeyClick: () -> Unit,
    onChatRoomClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: UserInfoViewModel = hiltViewModel()
) {
    val userData = viewModel.userData.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBackPressKeyClick) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "close button"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .fillMaxWidth()
        ) {
            GlideImage(imageModel = { /*TODO*/ })
        }
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GlideImage(
                imageModel = { userData.value?.picture?.ifBlank { R.drawable.icons8__ } },
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .sizeIn(minWidth = 20.dp, minHeight = 20.dp, maxWidth = 80.dp, maxHeight = 80.dp)
                    .clip(CircleShape),
                component = rememberImageComponent {
                    +ShimmerPlugin(
                        Shimmer.Flash(
                            baseColor = Color.White,
                            highlightColor = Color.LightGray
                        )
                    )
                }
            )
            Spacer(modifier = Modifier.height(60.dp))
            Column(
                modifier = Modifier.clickable { userData.value?.uid?.let { onChatRoomClick(it) } },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(imageVector = Icons.Outlined.Email, contentDescription = "message button")
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "채팅하기")
            }
            Spacer(modifier = Modifier.height(72.dp))
        }

    }
}

@Preview
@Composable
fun UserInfoScreenPreview(modifier: Modifier = Modifier) {
    MaterialTheme {
        UserInfoScreen(
            onBackPressKeyClick = { /*TODO*/ },
            onChatRoomClick = { /*TODO*/ },
            modifier = modifier
        )
    }
}