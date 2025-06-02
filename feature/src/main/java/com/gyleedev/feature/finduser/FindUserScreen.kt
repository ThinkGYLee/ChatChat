package com.gyleedev.feature.finduser

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.delete
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import com.gyleedev.domain.model.UserData
import com.gyleedev.feature.R
import com.skydoves.landscapist.components.rememberImageComponent
import com.skydoves.landscapist.glide.GlideImage
import com.skydoves.landscapist.placeholder.shimmer.Shimmer
import com.skydoves.landscapist.placeholder.shimmer.ShimmerPlugin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FindUserScreen(
    onBackPressKeyClick: () -> Unit,
    onProcessComplete: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FindUserViewModel = hiltViewModel()
) {
    val emailQuery = rememberTextFieldState()
    val emailIsAvailable = viewModel.emailIsAvailable.collectAsStateWithLifecycle()
    val userData = viewModel.userData.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(emailQuery.text) {
        viewModel.editEmail(emailQuery.text.toString())
    }
    val lifecycle = LocalLifecycleOwner.current
    LaunchedEffect(Unit) {
        // lifecycle

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

    LaunchedEffect(Unit) {
        viewModel.userProcessComplete
            .flowWithLifecycle(lifecycle.lifecycle)
            .collect { processState ->
                when (processState) {
                    FindProcessState.Complete -> {
                        onProcessComplete()
                    }

                    FindProcessState.AddFailure -> {
                        Toast.makeText(
                            context,
                            context.getString(R.string.add_user_failure_message),
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    FindProcessState.SearchFailure -> {
                        Toast.makeText(
                            context,
                            context.getString(R.string.search_user_failure_message),
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    FindProcessState.BlockFailure -> {
                        Toast.makeText(
                            context,
                            context.getString(R.string.block_user_failure_message),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
    }

    Scaffold(modifier = modifier, topBar = {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.find_user_screen_top_bar_text),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(
                    onClick = onBackPressKeyClick
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = stringResource(R.string.navigation_arrow_back_icon_description)
                    )
                }
            },
            actions = {
                TextButton(
                    onClick = viewModel::fetchUserData,
                    enabled = emailIsAvailable.value
                ) {
                    Text(text = stringResource(R.string.find_user_screen_action_button_text))
                }
            }
        )
    }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .fillMaxSize()
        ) {
            FindUserTextField(
                idQuery = emailQuery,
                onReset = {
                    emailQuery.edit { delete(0, emailQuery.text.length) }
                }
            )

            if (userData.value != null) {
                FindUserCard(
                    onAddAsFriend = viewModel::addFriend,
                    onBlockUser = viewModel::blockFriend,
                    userData = requireNotNull(userData.value)
                )
            }
        }
    }
}

@Composable
fun FindUserTextField(
    idQuery: TextFieldState,
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
                state = idQuery,
                lineLimits = TextFieldLineLimits.SingleLine,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email),
                decorator = { innerTextField ->
                    Row(
                        modifier = Modifier.padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(10f)) {
                            if (idQuery.text.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.find_user_text_field_hint),
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
                        if (idQuery.text.isNotEmpty()) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = stringResource(R.string.keyboard_reset_button_description),
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

@Composable
fun FindUserCard(
    onAddAsFriend: () -> Unit,
    onBlockUser: () -> Unit,
    userData: UserData,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(20.dp)
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GlideImage(
                imageModel = { userData.picture.ifBlank { R.drawable.baseline_person_24 } },
                modifier = Modifier
                    .size(
                        width = 80.dp,
                        height = 80.dp
                    )
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
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = userData.name)
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                OutlinedButton(onClick = onBlockUser) {
                    stringResource(R.string.block_friend_action)
                }
                Spacer(modifier = Modifier.width(20.dp))
                Button(onClick = onAddAsFriend) {
                    Text(text = stringResource(R.string.find_user_screen_add_button_text))
                }
            }
        }
    }
}

@Composable
@Preview
fun FindUserScreenPreview() {
    MaterialTheme {
        FindUserScreen(onProcessComplete = {}, onBackPressKeyClick = {})
    }
}
