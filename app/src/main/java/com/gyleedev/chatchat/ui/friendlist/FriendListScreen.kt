package com.gyleedev.chatchat.ui.friendlist

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import com.gyleedev.chatchat.R
import com.gyleedev.chatchat.domain.FriendData
import com.gyleedev.chatchat.domain.UserData
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.components.rememberImageComponent
import com.skydoves.landscapist.glide.GlideImage
import com.skydoves.landscapist.placeholder.shimmer.Shimmer
import com.skydoves.landscapist.placeholder.shimmer.ShimmerPlugin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendListScreen(
    onMyInfoClick: (String) -> Unit,
    onFriendClick: (String) -> Unit,
    onFindUserButtonClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FriendListViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.fetchState.collect {
        }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.fetchMyUserData()
    }

    val myUserData = viewModel.myUserData.collectAsStateWithLifecycle()
    val items = viewModel.items.collectAsLazyPagingItems()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.friend_list_screen_top_bar_title)) },
                actions = {
                    IconButton(onClick = onFindUserButtonClick) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = stringResource(R.string.add_friend_button_description)
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            if (myUserData.value != null) {
                MyUserData(
                    onClick = { if (myUserData.value != null) onMyInfoClick(myUserData.value!!.uid) },
                    userData = myUserData.value!!
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.friend_list_screen_middle_title),
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = items.itemCount.toString(),
                    style = MaterialTheme.typography.labelMedium
                )
            }
            if (items.itemCount > 0) {
                LazyColumn(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(vertical = 12.dp)
                ) {
                    items(
                        items.itemCount,
                        key = { items[it]!!.email },
                        contentType = { 0 }
                    ) { index ->
                        val friend = items[index] as FriendData
                        FriendData(onClick = { onFriendClick(friend.uid) }, friendData = friend)
                    }
                }
            }
        }
    }
}

@Composable
fun MyUserData(
    onClick: () -> Unit,
    userData: UserData,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GlideImage(
            imageModel = {
                userData.picture.ifBlank { R.drawable.icons8__ }
            },
            imageOptions = ImageOptions(
                contentScale = ContentScale.Crop
            ),
            modifier = Modifier
                .size(60.dp)
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
        //GlideImage(imageModel = { userData.picture.ifBlank { R.drawable.icons8__ } })
        Spacer(modifier = Modifier.width(20.dp))
        Column(verticalArrangement = Arrangement.Center) {
            Text(text = userData.name)
            if (userData.status.isNotBlank()) {
                Text(
                    text = userData.status,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Composable
fun FriendData(
    onClick: () -> Unit,
    friendData: FriendData,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GlideImage(imageModel = { friendData.picture.ifBlank { R.drawable.icons8__ } })
        Spacer(modifier = Modifier.width(20.dp))
        Column(horizontalAlignment = Alignment.Start) {
            Text(text = friendData.name, style = MaterialTheme.typography.bodyMedium)
            if (friendData.status.isNotBlank()) {
                Text(
                    text = friendData.status,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}
