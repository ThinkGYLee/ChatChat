package com.gyleedev.chatchat.ui.friendlist

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.outlined.PersonAdd
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
import androidx.compose.ui.res.colorResource
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
import com.gyleedev.chatchat.domain.RelatedUserLocalData
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
    onEditFriendClick: () -> Unit,
    onManageFriendClick: () -> Unit,
    onSettingClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FriendListViewModel = hiltViewModel()
) {
    var openFriendDialog by remember { mutableStateOf(false) }
    var dialogRelatedUserLocalData by remember { mutableStateOf<RelatedUserLocalData?>(null) }
    val lifecycle = LocalLifecycleOwner.current
    val context = LocalContext.current
    val friends = viewModel.getFriends.collectAsLazyPagingItems()
    val favorites = viewModel.getFavorites.collectAsLazyPagingItems()
    val myData by viewModel.myUserData.collectAsStateWithLifecycle()
    var dropdownMenuExpanded by rememberSaveable { mutableStateOf(false) }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.getMyUserFromPreference()
    }

    LaunchedEffect(Unit) {
        viewModel.fetchState.collect {
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
                            imageVector = Icons.Outlined.PersonAdd,
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
            item {
                AnimatedVisibility(myData != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        MyUserData(
                            onClick = {
                                onMyInfoClick(requireNotNull(myData).uid)
                            },
                            userData = requireNotNull(myData)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            thickness = 0.3.dp
                        )
                    }
                }
            }

            item {
                AnimatedVisibility(favorites.itemCount > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                            .padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = "즐겨찾기 ${favorites.itemCount}",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }

            items(
                count = favorites.itemCount,
                key = { "${requireNotNull(favorites[it]).id}+favorites" },
                contentType = { 0 }
            ) {
                AnimatedVisibility(favorites.itemCount > 0) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        FriendData(
                            onClick = { onFriendClick(requireNotNull(favorites[it]).uid) },
                            onLongClick = {
                                dialogRelatedUserLocalData = favorites[it]
                                openFriendDialog = true
                            },
                            relatedUserLocalData = requireNotNull(favorites[it]),
                            modifier = Modifier.animateItem()
                        )

                        if (it == favorites.itemCount - 1) {
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 20.dp),
                                thickness = 0.3.dp
                            )
                        }
                    }
                }
            }

            item {
                AnimatedVisibility(friends.itemCount > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(
                            vertical = 8.dp,
                            horizontal = 20.dp
                        )
                    ) {
                        Text(
                            text = "친구 ${friends.itemCount}",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }

            items(
                count = friends.itemCount,
                key = { "${requireNotNull(friends[it]).id}+friends" },
                contentType = { 0 }
            ) {
                AnimatedVisibility(friends.itemCount > 0) {
                    FriendData(
                        onClick = { onFriendClick(requireNotNull(friends[it]).uid) },
                        onLongClick = {
                            dialogRelatedUserLocalData = friends[it]
                            openFriendDialog = true
                        },
                        relatedUserLocalData = requireNotNull(friends[it]),
                        modifier = Modifier.animateItem()
                    )
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
            imageModel = { imageUrl.ifBlank { R.drawable.baseline_person_24 } },
            imageOptions = ImageOptions(contentScale = ContentScale.Crop),
            modifier = Modifier
                .size(56.dp)
                .border(
                    width = 0.01.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(20.dp)
                )
                .clip(RoundedCornerShape(20.dp))
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
        Box(modifier = Modifier.wrapContentSize()) {
            GlideImage(
                imageModel = { imageUrl.ifBlank { R.drawable.baseline_person_24 } },
                imageOptions = ImageOptions(contentScale = ContentScale.Crop),
                modifier = Modifier
                    .size(40.dp)
                    .border(
                        width = 0.01.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clip(RoundedCornerShape(16.dp))
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
        }
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
