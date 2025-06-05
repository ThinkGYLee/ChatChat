package com.gyleedev.feature.signin

import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gyleedev.domain.model.SignInResult
import com.gyleedev.feature.R
import com.gyleedev.feature.component.TextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SigninScreen(
    onSignInComplete: () -> Unit,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SigninViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var isLastTextFieldFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val columnScrollState = rememberScrollState()

    LaunchedEffect(isLastTextFieldFocused) {
        if (isLastTextFieldFocused) {
            columnScrollState.animateScrollBy(100f)
        }
    }

    val idComment =
        if (uiState is SigninUiState.Loading || (uiState as SigninUiState.Success).idIsAvailable || (uiState as SigninUiState.Success).idQuery.isEmpty()) {
            ""
        } else {
            stringResource(R.string.email_format_error)
        }

    val nickNameComment =
        if (uiState is SigninUiState.Loading || (uiState as SigninUiState.Success).nicknameIsAvailable || (uiState as SigninUiState.Success).nicknameQuery.isEmpty()) {
            ""
        } else {
            stringResource(R.string.nickname_length_error)
        }

    val passwordComment =
        if (uiState is SigninUiState.Loading || (uiState as SigninUiState.Success).passwordIsAvailable || (uiState as SigninUiState.Success).passwordQuery.isEmpty()) {
            stringResource(R.string.blank_text)
        } else {
            stringResource(R.string.password_available_text)
        }
    val passwordCheckComment =
        if (uiState is SigninUiState.Loading || (uiState as SigninUiState.Success).passwordIsSame || (uiState as SigninUiState.Success).passwordCheckQuery.isEmpty()) {
            stringResource(R.string.blank_text)
        } else {
            stringResource(R.string.password_check_available_text)
        }

    LaunchedEffect(Unit) {
        viewModel.signInProgress.collect {
            if (it == SignInResult.Success) {
                snackbarHostState.showSnackbar(
                    message = context.getString(R.string.sign_in_success_message),
                    duration = SnackbarDuration.Short
                )
                onSignInComplete()
            } else {
                snackbarHostState.showSnackbar(
                    message = context.getString(R.string.sign_in_failure_message),
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.signin_screen_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.navigation_arrow_back_icon_description)
                        )
                    }
                }
            )
        },
        modifier = modifier,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            if (uiState is SigninUiState.Success) {
                Column(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .imePadding()
                ) {
                    Button(
                        enabled = (uiState as SigninUiState.Success).signinIsAvailable,
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.signInRequest()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.signin_button_title),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    ) { innerPadding ->
        if (uiState is SigninUiState.Success) {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(columnScrollState)
            ) {
                Text(text = stringResource(R.string.id_text_field_hint))
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    value = (uiState as SigninUiState.Success).idQuery,
                    onValueChange = { viewModel.editId(it) },
                    hint = stringResource(R.string.id_text_field_hint),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next,
                        keyboardType = KeyboardType.Email
                    ),
                    modifier = Modifier.focusRequester(focusRequester)
                )
                Text(
                    text = idComment,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Red
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(text = stringResource(R.string.nickname_text_field_hint))
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    value = (uiState as SigninUiState.Success).nicknameQuery,
                    onValueChange = { viewModel.editNickname(it) },
                    hint = stringResource(R.string.nickname_text_field_hint),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.focusRequester(focusRequester)
                )
                Text(
                    text = nickNameComment,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Red
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(text = stringResource(R.string.password_text_field_hint))
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    value = (uiState as SigninUiState.Success).passwordQuery,
                    onValueChange = { viewModel.editPassword(it) },
                    hint = stringResource(R.string.password_text_field_hint),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next,
                        keyboardType = KeyboardType.Password
                    ),
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.focusRequester(focusRequester)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = passwordComment,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Red
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = stringResource(R.string.password_check_text_field_hint))
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    value = (uiState as SigninUiState.Success).passwordCheckQuery,
                    onValueChange = { viewModel.editPasswordCheck(it) },
                    hint = stringResource(R.string.password_check_text_field_hint),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done,
                        keyboardType = KeyboardType.Password
                    ),
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier
                        .focusRequester(focusRequester)
                        .onFocusChanged {
                            isLastTextFieldFocused = it.isFocused
                        }
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = passwordCheckComment,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Red
                )
                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }
}
