package com.gyleedev.chatchat.ui.myinfoedit

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import com.gyleedev.chatchat.R
import com.gyleedev.chatchat.util.getImageFromFireStore
import com.skydoves.landscapist.components.rememberImageComponent
import com.skydoves.landscapist.glide.GlideImage
import com.skydoves.landscapist.placeholder.shimmer.Shimmer
import com.skydoves.landscapist.placeholder.shimmer.ShimmerPlugin
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyInfoEditScreen(
    onBackKeyPressed: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MyInfoEditViewModel = hiltViewModel()
) {
    val myData by viewModel.myUserData.collectAsStateWithLifecycle()
    val myName by viewModel.myNameQuery.collectAsStateWithLifecycle()
    val myStatus by viewModel.myStatusQuery.collectAsStateWithLifecycle()
    val myPicture by viewModel.myPictureAddress.collectAsStateWithLifecycle()
    val lifecycle = LocalLifecycleOwner.current
    val context = LocalContext.current

    val pickMedia =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            // Callback is invoked after the user selects a media item or closes the
            // photo picker.
            if (uri != null) {
                viewModel.changePictureUri(uri)
            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }

    LaunchedEffect(Unit) {
        viewModel.request
            .flowWithLifecycle(lifecycle.lifecycle)
            .collect {
                if (it) {
                    onBackKeyPressed()
                } else {
                    Toast.makeText(
                        context,
                        context.getString(R.string.search_user_failure_message),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBackKeyPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.navigation_arrow_back_icon_description)
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = viewModel::updateMyInfo,
                        enabled = myName.isNotEmpty()
                    ) {
                        Text(stringResource(R.string.action_button_complete_message))
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 60.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            var imageUrl by rememberSaveable { mutableStateOf("") }
            LaunchedEffect(myData) {
                if (requireNotNull(myData?.picture).isNotEmpty()) {
                    imageUrl = getImageFromFireStore(myPicture).first()
                    viewModel.changePictureUri(imageUrl.toUri())
                }
            }

            Box {
                GlideImage(
                    imageModel = { myPicture.ifBlank { R.drawable.icons8__ } },
                    modifier = Modifier
                        .sizeIn(
                            maxWidth = 80.dp,
                            maxHeight = 80.dp
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
                Box(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.background,
                            shape = CircleShape
                        )
                        .align(Alignment.BottomEnd)
                        .clickable {
                            pickMedia.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = ""
                    )
                }
            }

            Spacer(modifier = Modifier.height(60.dp))
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    stringResource(R.string.edit_name_hint),
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.labelSmall
                )
                Row {
                    MyEditTextField(
                        myName,
                        onValueChange = viewModel::changeNameQuery,
                        onReset = { viewModel.changeNameQuery("") }
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    stringResource(R.string.edit_status_hint),
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.labelSmall
                )
                Row {
                    MyEditTextField(
                        myStatus,
                        onValueChange = viewModel::changeStatusQuery,
                        onReset = { viewModel.changeStatusQuery("") }
                    )
                }
            }
            Spacer(modifier = Modifier.height(160.dp))
        }
    }
}

@Composable
fun MyEditTextField(
    query: String,
    onValueChange: (String) -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    var alpha by remember { mutableFloatStateOf(1f) }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            BasicTextField(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .onFocusChanged { alpha = if (it.isFocused) 0.6f else 1f }
                    .padding(horizontal = 16.dp),
                value = query,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                decorationBox = { innerTextField ->
                    Row(
                        modifier = Modifier.padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(10f)) {
                            if (query.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.edit_status_hint),
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
                        if (query.isNotEmpty()) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = stringResource(R.string.edit_query_reset_description),
                                modifier = Modifier.clickable { onReset() }
                            )
                        }
                    }
                }
            )
            Spacer(modifier = Modifier.height(4.dp))
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        }
    }
}
