package com.gyleedev.feature.login

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gyleedev.domain.model.LogInState
import com.gyleedev.feature.R
import com.gyleedev.feature.component.TextField

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLogInComplete: () -> Unit,
    onSignInClicked: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val idComment =
        if (uiState is LoginUiState.Loading || (uiState as LoginUiState.Success).idIsAvailable || (uiState as LoginUiState.Success).idQuery.isEmpty()) {
            ""
        } else {
            stringResource(R.string.id_incorrect_message)
        }
    val passwordComment =
        if (uiState is LoginUiState.Loading || (uiState as LoginUiState.Success).passwordIsAvailable || (uiState as LoginUiState.Success).passwordQuery.isEmpty()) {
            ""
        } else {
            stringResource(R.string.password_incorrect_message)
        }
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        viewModel.logInResult.collect {
            if (it is LogInState.Success) {
                snackbarHostState.showSnackbar(
                    message = context.getString(R.string.log_in_success_message),
                    duration = SnackbarDuration.Short,
                )
                onLogInComplete()
            } else {
                snackbarHostState.showSnackbar(
                    message = context.getString(R.string.log_in_failure_message),
                    duration = SnackbarDuration.Short,
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.login_screen_top_bar_title)) },
            )
        },
        bottomBar = {
            if (uiState is LoginUiState.Success) {
                val state = uiState as LoginUiState.Success
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .imePadding(),
                ) {
                    Button(
                        enabled = state.loginIsAvailable,
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.logInButtonClick()
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors().copy(
                            disabledContainerColor = ButtonDefaults.buttonColors().containerColor.copy(
                                alpha = 0.2f,
                            ),
                            disabledContentColor = MaterialTheme.colorScheme.onSurface,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.login_screen_login_button_text),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onSignInClicked,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors().copy(
                            containerColor = ButtonDefaults.buttonColors().disabledContainerColor.copy(),
                            contentColor = MaterialTheme.colorScheme.onSurface,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.login_screen_sign_in_text),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = modifier,
    ) { innerPadding ->
        if (uiState is LoginUiState.Success) {
            val state = uiState as LoginUiState.Success
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
            ) {
                Spacer(modifier = Modifier.height(20.dp))
                TextField(
                    value = state.idQuery,
                    onValueChange = {
                        viewModel.editId(it)
                    },
                    hint = stringResource(R.string.id_text_field_hint),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next,
                        keyboardType = KeyboardType.Email,
                    ),
                )
                Text(
                    text = idComment,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Red,
                )
                Spacer(modifier = Modifier.height(20.dp))
                TextField(
                    value = state.passwordQuery,
                    onValueChange = {
                        viewModel.editPassword(it)
                    },
                    hint = stringResource(R.string.password_text_field_hint),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done,
                        keyboardType = KeyboardType.Password,
                    ),
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.focusRequester(focusRequester),
                )
                Text(
                    text = passwordComment,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Red,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Forgot your username or password?",
                    textDecoration = TextDecoration.Underline,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .clickable {},

                )
            }
        }
    }
}
