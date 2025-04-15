package com.gyleedev.chatchat.ui.friendlist

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import com.gyleedev.chatchat.R
import com.gyleedev.chatchat.domain.FriendData
import com.gyleedev.chatchat.domain.UserData
import com.gyleedev.chatchat.util.getImageFromFireStore
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.components.rememberImageComponent
import com.skydoves.landscapist.glide.GlideImage
import com.skydoves.landscapist.placeholder.shimmer.Shimmer
import com.skydoves.landscapist.placeholder.shimmer.ShimmerPlugin
import kotlinx.coroutines.flow.first

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

    var openFriendDialog by remember { mutableStateOf(false) }
    var dialogFriendData by remember { mutableStateOf<FriendData?>(null) }
    val lifecycle = LocalLifecycleOwner.current
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.noSuchUserAlert
            .flowWithLifecycle(lifecycle.lifecycle)
            .collect {
                Toast.makeText(context, "no such user", Toast.LENGTH_SHORT).show()
            }
    }

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
        ) {
            if (myUserData.value != null) {
                MyUserData(
                    onClick = { if (myUserData.value != null) onMyInfoClick(myUserData.value!!.uid) },
                    userData = myUserData.value!!
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 20.dp)
            ) {
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
                        FriendData(
                            onClick = { onFriendClick(friend.uid) },
                            onLongClick = {
                                dialogFriendData = friend
                                openFriendDialog = true
                            },
                            friendData = friend
                        )
                    }
                }
            }

            if (openFriendDialog) {
                FriendDialog(
                    closeDialog = {
                        dialogFriendData = null
                        openFriendDialog = false
                    },
                    blockRequest = {},
                    deleteRequest = {
                        viewModel.deleteFriend(dialogFriendData)
                    },
                    hideRequest = {}
                )
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
    var imageUrl by rememberSaveable {
        mutableStateOf("")
    }
    LaunchedEffect(userData) {
        imageUrl = getImageFromFireStore(userData.picture).first()
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GlideImage(
            imageModel = {
                imageUrl.ifBlank { R.drawable.icons8__ }
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FriendData(
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    friendData: FriendData,
    modifier: Modifier = Modifier
) {
    var imageUrl by rememberSaveable {
        mutableStateOf("")
    }
    LaunchedEffect(friendData) {
        imageUrl = getImageFromFireStore(friendData.picture).first()
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onLongClick = onLongClick,
                onClick = onClick
            )
            .padding(vertical = 8.dp, horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GlideImage(
            imageModel = {
                imageUrl.ifBlank { R.drawable.icons8__ }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendDialog(
    blockRequest: () -> Unit,
    deleteRequest: () -> Unit,
    hideRequest: () -> Unit,
    closeDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    BasicAlertDialog(
        onDismissRequest = closeDialog,
        content = {
            Surface(
                modifier = Modifier.wrapContentSize(),
                shape = MaterialTheme.shapes.medium,
                tonalElevation = AlertDialogDefaults.TonalElevation
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TextButton(
                        onClick = {
                            blockRequest()
                            closeDialog()
                        }
                    ) {
                        Text(stringResource(R.string.friend_block_button_text))
                    }
                    HorizontalDivider()
                    TextButton(
                        onClick = {
                            deleteRequest()
                            closeDialog()
                        }
                    ) {
                        Text(stringResource(R.string.friend_delete_button_text))
                    }
                    HorizontalDivider()
                    TextButton(
                        onClick = {
                            hideRequest()
                            closeDialog()
                        }
                    ) {
                        Text(stringResource(R.string.friend_hide_button_text))
                    }
                }
            }
        },
        modifier = modifier.wrapContentSize()
    )
}
