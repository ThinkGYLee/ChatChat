package com.gyleedev.chatchat.ui.friendlist

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import com.gyleedev.chatchat.R
import com.gyleedev.chatchat.domain.RelatedUserLocalData
import com.gyleedev.chatchat.domain.UserData
import com.gyleedev.chatchat.util.getImageFromFireStore
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.components.rememberImageComponent
import com.skydoves.landscapist.glide.GlideImage
import com.skydoves.landscapist.placeholder.shimmer.Shimmer
import com.skydoves.landscapist.placeholder.shimmer.ShimmerPlugin
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendListScreen(
    onMyInfoClick: (String) -> Unit,
    onFriendClick: (String) -> Unit,
    onFindUserButtonClick: () -> Unit,
    onEditFriendClick: () -> Unit,
    onManageFriendClick: () -> Unit,
    onSettingClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FriendListViewModel = hiltViewModel()
) {
    val items = viewModel.items.collectAsLazyPagingItems()

    var openFriendDialog by remember { mutableStateOf(false) }
    var dialogRelatedUserLocalData by remember { mutableStateOf<RelatedUserLocalData?>(null) }
    val lifecycle = LocalLifecycleOwner.current
    val context = LocalContext.current

    var dropdownMenuExpanded by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.fetchState.collect {
        }
    }

    LaunchedEffect(Unit) {
        viewModel.fetchJobDone
            .flowWithLifecycle(lifecycle.lifecycle).collectLatest {
                items.refresh()
            }
    }

    LaunchedEffect(Unit) {
        viewModel.noSuchUserAlert
            .flowWithLifecycle(lifecycle.lifecycle)
            .collect {
                Toast.makeText(context, R.string.search_result_no_user_text, Toast.LENGTH_SHORT).show()
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

                    IconButton(onClick = { dropdownMenuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = stringResource(R.string.friend_list_setting_button_description)
                        )
                    }

                    FriendManagementDropDownMenu(
                        dropdownMenuExpanded = dropdownMenuExpanded,
                        onDismiss = { dropdownMenuExpanded = false },
                        editRequest = onEditFriendClick,
                        manageFriendRequest = onManageFriendClick,
                        settingRequest = onSettingClick
                    )
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            items(
                items.itemCount,
                key = { it },
                contentType = { 0 }
            ) { index ->
                when (items[index]) {
                    is FriendListUiState.MyData -> {
                        val myData = items[index] as FriendListUiState.MyData
                        MyUserData(
                            onClick = { onMyInfoClick(requireNotNull(myData.myData).uid) },
                            userData = requireNotNull(myData.myData)
                        )
                    }

                    is FriendListUiState.Title -> {
                        val titleData = items[index] as FriendListUiState.Title
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        ) {
                            Text(
                                text = titleData.text,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }

                    is FriendListUiState.FriendData -> {
                        val friendData = items[index] as FriendListUiState.FriendData
                        FriendData(
                            onClick = { onFriendClick(friendData.friendData.uid) },
                            onLongClick = {
                                dialogRelatedUserLocalData = friendData.friendData
                                openFriendDialog = true
                            },
                            relatedUserLocalData = friendData.friendData
                        )
                    }

                    is FriendListUiState.FavoriteData -> {
                        val favoriteData = items[index] as FriendListUiState.FavoriteData
                        FriendData(
                            onClick = { onFriendClick(favoriteData.favoriteData.uid) },
                            onLongClick = {
                                dialogRelatedUserLocalData = favoriteData.favoriteData
                                openFriendDialog = true
                            },
                            relatedUserLocalData = favoriteData.favoriteData
                        )
                    }

                    else -> {}
                }
            }
        }
    }
    if (openFriendDialog) {
        FriendDialog(
            closeDialog = {
                dialogRelatedUserLocalData = null
                openFriendDialog = false
            },
            blockRequest = { viewModel.blockFriend(dialogRelatedUserLocalData) },
            deleteRequest = { viewModel.deleteFriend(dialogRelatedUserLocalData) },
            hideRequest = { viewModel.hideFriend(dialogRelatedUserLocalData) }
        )
    }
}

@Composable
fun MyUserData(
    onClick: () -> Unit,
    userData: UserData,
    modifier: Modifier = Modifier
) {
    var imageUrl by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(userData) { imageUrl = getImageFromFireStore(userData.picture).first() }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GlideImage(
            imageModel = { imageUrl.ifBlank { R.drawable.icons8__ } },
            imageOptions = ImageOptions(contentScale = ContentScale.Crop),
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
    relatedUserLocalData: RelatedUserLocalData,
    modifier: Modifier = Modifier
) {
    var imageUrl by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(relatedUserLocalData) {
        imageUrl = getImageFromFireStore(relatedUserLocalData.picture).first()
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
            imageModel = { imageUrl.ifBlank { R.drawable.icons8__ } },
            imageOptions = ImageOptions(contentScale = ContentScale.Crop),
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
            Text(text = relatedUserLocalData.name, style = MaterialTheme.typography.bodyMedium)
            if (relatedUserLocalData.status.isNotBlank()) {
                Text(
                    text = relatedUserLocalData.status,
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

@Composable
fun FriendManagementDropDownMenu(
    editRequest: () -> Unit,
    manageFriendRequest: () -> Unit,
    settingRequest: () -> Unit,
    dropdownMenuExpanded: Boolean,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit
) {
    DropdownMenu(
        expanded = dropdownMenuExpanded,
        modifier = modifier,
        onDismissRequest = onDismiss
    ) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.edit_button_text)) },
            onClick = {
                editRequest()
                onDismiss()
            }
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.manage_friend_button_text)) },
            onClick = {
                manageFriendRequest()
                onDismiss()
            }
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.entire_setting_button_text)) },
            onClick = {
                settingRequest()
                onDismiss()
            }
        )
    }
}
