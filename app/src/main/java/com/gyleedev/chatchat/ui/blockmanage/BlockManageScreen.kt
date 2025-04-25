package com.gyleedev.chatchat.ui.blockmanage

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import com.gyleedev.chatchat.R
import com.gyleedev.chatchat.domain.RelatedUserLocalData
import com.gyleedev.chatchat.domain.UserRelationState
import com.gyleedev.chatchat.util.getImageFromFireStore
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.components.rememberImageComponent
import com.skydoves.landscapist.glide.GlideImage
import com.skydoves.landscapist.placeholder.shimmer.Shimmer
import com.skydoves.landscapist.placeholder.shimmer.ShimmerPlugin
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockManageScreen(
    onBackPressKeyClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BlockManageViewModel = hiltViewModel()
) {
    val searchQuery = viewModel.searchQuery.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val items = viewModel.items.collectAsLazyPagingItems()
    val searchItems = viewModel.searchItems.collectAsLazyPagingItems()

    val lifecycle = LocalLifecycleOwner.current
    LaunchedEffect(Unit) {
        viewModel.searchFailure
            .flowWithLifecycle(lifecycle.lifecycle)
            .collect {
                Toast.makeText(
                    context,
                    context.getString(R.string.search_user_failure_message),
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.block_manage_screen_title),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressKeyClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.navigation_arrow_back_icon_description)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            FriendFilterTextField(
                searchQuery = searchQuery.value,
                onReset = { viewModel.editSearchQuery("") },
                onValueChange = viewModel::editSearchQuery
            )
            Spacer(modifier = Modifier.height(20.dp))

            if (items.itemCount > 0) {
                AnimatedVisibility(searchQuery.value.isEmpty()) {
                    Column {
                        Text(
                            text = stringResource(R.string.block_manage_screen_friend_text),
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 20.dp)
                        )
                        LazyColumn {
                            items(
                                items.itemCount,
                                key = { requireNotNull(items[it]).email },
                                contentType = { 0 }
                            ) {
                                items[it]?.let { it1 ->
                                    FriendData(
                                        relatedUserLocalData = it1,
                                        onHideRequest = { viewModel.userToFriend(it1) }
                                    )
                                }
                            }
                        }
                    }
                }
                AnimatedVisibility(searchQuery.value.isNotEmpty()) {
                    Column {
                        Text(
                            text = stringResource(R.string.search_result_text),
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 20.dp)
                        )
                        LazyColumn {
                            items(
                                count = searchItems.itemCount,
                                key = { requireNotNull(searchItems[it]).email },
                                contentType = { 0 }
                            ) {
                                searchItems[it]?.let { it1 ->
                                    FriendData(
                                        relatedUserLocalData = it1,
                                        onHideRequest = { viewModel.userToFriend(it1) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FriendFilterTextField(
    searchQuery: String,
    onValueChange: (String) -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    var alpha by remember { mutableFloatStateOf(1f) }

    Row(
        modifier = modifier.padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            BasicTextField(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .onFocusChanged { alpha = if (it.isFocused) 0.6f else 1f }
                    .padding(horizontal = 16.dp),
                value = searchQuery,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email),
                decorationBox = { innerTextField ->
                    Row(
                        modifier = Modifier.padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(10f)) {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.friend_filter_text_field_text_hint),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color(0xFF848484),
                                    modifier = Modifier
                                        .padding(start = 4.dp)
                                        .align(Alignment.CenterStart)
                                )
                            }
                            Row(modifier = Modifier.align(Alignment.CenterStart)) {
                                innerTextField()
                            }
                        }
                        if (searchQuery.isNotEmpty()) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = stringResource(R.string.keyboard_reset_button_description),
                                modifier = Modifier.clickable { onReset() }
                            )
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun FriendData(
    onHideRequest: () -> Unit,
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
            .clickable { }
            .padding(vertical = 8.dp, horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
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
            Text(
                text = relatedUserLocalData.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
        }

        TextButton(
            border = BorderStroke(
                0.5.dp,
                color = MaterialTheme.colorScheme.onSurface
            ),
            onClick = onHideRequest
        ) {
            Text(text = stringResource(R.string.block_manage_screen_unblock_text))
        }
    }
}

@Preview
@Composable
fun FriendDataPreview() {
    MaterialTheme {
        FriendData(
            relatedUserLocalData = RelatedUserLocalData(
                id = 0,
                email = "email",
                name = "name",
                status = "status",
                picture = "",
                uid = "",
                userRelation = UserRelationState.FRIEND
            ),
            onHideRequest = {}
        )
    }
}

@PreviewLightDark
@Composable
@Preview
fun FriendFilterTextFieldPreview() {
    MaterialTheme {
        FriendFilterTextField(
            searchQuery = "",
            onReset = {},
            onValueChange = {}
        )
    }
}
