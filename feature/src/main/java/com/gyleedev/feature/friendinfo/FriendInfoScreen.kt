package com.gyleedev.feature.friendinfo

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gyleedev.feature.R
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
    val friendData by viewModel.relatedUserLocalData.collectAsStateWithLifecycle()
    val starColor = if (requireNotNull(friendData).favoriteState) {
        Color.Yellow
    } else {
        Color.Transparent
    }

    var dropdownMenuExpanded by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onCloseKeyPressed) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = stringResource(R.string.close_button_description)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.updateFavorite() }
                    ) {
                        // default size 24dp
                        Icon(
                            imageVector = Icons.Default.StarOutline,
                            contentDescription = null
                        )

                        // adjust size 18dp
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = starColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(onClick = { dropdownMenuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Outlined.MoreVert,
                            contentDescription = stringResource(R.string.more_vertical_button_description)
                        )
                    }

                    FriendDropDownMenu(
                        dropdownMenuExpanded = dropdownMenuExpanded,
                        onDismiss = { dropdownMenuExpanded = false },
                        blockRequest = { viewModel.blockFriend() },
                        deleteRequest = { viewModel.deleteFriend() },
                        hideRequest = { viewModel.hideFriend() }
                    )
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
                imageModel = { requireNotNull(friendData).picture.ifBlank { R.drawable.baseline_person_24 } },
                modifier = Modifier
                    .size(120.dp)
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
            Spacer(modifier = Modifier.height(20.dp))
            Text(text = friendData?.name ?: stringResource(R.string.anonymous_user_text))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = friendData?.status ?: stringResource(R.string.blank_text))
            Spacer(modifier = Modifier.height(60.dp))
            Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Column(
                    modifier = Modifier.clickable { friendData?.let { onChatRoomClick(it.uid) } },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Email,
                        contentDescription = stringResource(R.string.message_button_description)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = stringResource(R.string.message_button_message))
                }
            }
            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}

@Composable
fun FriendDropDownMenu(
    blockRequest: () -> Unit,
    deleteRequest: () -> Unit,
    hideRequest: () -> Unit,
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
            text = { Text(stringResource(R.string.friend_block_button_text)) },
            onClick = {
                blockRequest()
                onDismiss()
            }
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.friend_delete_button_text)) },
            onClick = {
                deleteRequest()
                onDismiss()
            }
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.friend_hide_button_text)) },
            onClick = {
                hideRequest()
                onDismiss()
            }
        )
    }
}
