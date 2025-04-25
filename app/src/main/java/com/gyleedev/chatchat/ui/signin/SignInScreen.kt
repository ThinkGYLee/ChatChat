package com.gyleedev.chatchat.ui.signin

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicSecureTextField
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.delete
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gyleedev.chatchat.R
import com.gyleedev.chatchat.domain.SignInResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignInScreen(
    onSignInComplete: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SignInViewModel = hiltViewModel()
) {
    val idQuery = rememberTextFieldState()
    val nicknameQuery = rememberTextFieldState()
    val passwordQuery = rememberTextFieldState()
    val passwordCheckQuery = rememberTextFieldState()
    val signInIsAvailable = viewModel.signInIsAvailable.collectAsStateWithLifecycle()
    val idIsAvailable = viewModel.idIsAvailable.collectAsStateWithLifecycle()
    val nicknameIsAvailable = viewModel.nicknameIsAvailable.collectAsStateWithLifecycle()
    val passwordIsAvailable = viewModel.passwordIsAvailable.collectAsStateWithLifecycle()
    val passwordIsSame = viewModel.passwordIsSame.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(idQuery.text) {
        viewModel.editId(idQuery.text.toString())
    }

    LaunchedEffect(nicknameQuery.text) {
        viewModel.editNickname(nicknameQuery.text.toString())
    }

    LaunchedEffect(passwordQuery.text) {
        viewModel.editPassword(passwordQuery.text.toString())
    }

    LaunchedEffect(passwordCheckQuery.text) {
        viewModel.editPasswordCheck(passwordCheckQuery.text.toString())
    }

    LaunchedEffect(Unit) {
        viewModel.fetchState.collect {
        }
    }

    LaunchedEffect(Unit) {
        viewModel.signInProgress.collect {
            if (it == SignInResult.Success) {
                Toast.makeText(
                    context,
                    context.getString(R.string.sign_in_success_message),
                    Toast.LENGTH_SHORT
                ).show()
                onSignInComplete()
            } else {
                Toast.makeText(
                    context,
                    context.getString(R.string.sign_in_failure_message),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.signin_screen_title)) }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            IdScreen(
                idQuery = idQuery,
                idIsAvailable = idIsAvailable.value,
                onReset = {
                    idQuery.edit { delete(0, idQuery.text.length) }
                }
            )
            Spacer(modifier = Modifier.height(20.dp))

            NicknameScreen(
                nicknameQuery = nicknameQuery,
                nickNameIsAvailable = nicknameIsAvailable.value,
                onReset = {
                    nicknameQuery.edit { delete(0, nicknameQuery.text.length) }
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            PasswordScreen(
                passwordQuery = passwordQuery,
                passwordCheckQuery = passwordCheckQuery,
                passwordIsAvailable = passwordIsAvailable.value,
                passwordIsSame = passwordIsSame.value,
                onPasswordReset = {
                    passwordQuery.edit { delete(0, passwordQuery.text.length) }
                },
                onPasswordCheckReset = {
                    passwordCheckQuery.edit { delete(0, passwordCheckQuery.text.length) }
                }
            )

            Button(
                enabled = signInIsAvailable.value,
                onClick = viewModel::signInRequest,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        shape = RoundedCornerShape(8.dp),
                        color = ButtonDefaults.buttonColors().containerColor
                    )
            ) {
                Text(
                    text = stringResource(R.string.signin_button_title),
                    style = MaterialTheme.typography.bodyLarge,
                    color = ButtonDefaults.buttonColors().contentColor
                )
            }
        }
    }
}

@Composable
fun IdTextField(
    idQuery: TextFieldState,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    var alpha by remember { mutableFloatStateOf(1f) }

    Row(
        modifier = modifier.border(0.1.dp, MaterialTheme.colorScheme.onSurface),
        verticalAlignment = Alignment.CenterVertically
    ) {
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
            decorator = { innerTextField ->
                Row(
                    modifier = Modifier.padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(10f)) {
                        if (idQuery.text.isEmpty()) {
                            Text(
                                text = stringResource(R.string.id_text_field_hint),
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
            },
            cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface)
        )
    }
}

@Composable
fun NicknameTextField(
    nicknameQuery: TextFieldState,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    var alpha by remember { mutableFloatStateOf(1f) }

    Row(
        modifier = modifier.border(0.1.dp, MaterialTheme.colorScheme.onSurface),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicTextField(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(8.dp)
                )
                .onFocusChanged { alpha = if (it.isFocused) 0.6f else 1f }
                .padding(horizontal = 16.dp),
            state = nicknameQuery,
            lineLimits = TextFieldLineLimits.SingleLine,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
            decorator = { innerTextField ->
                Row(
                    modifier = Modifier.padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(10f)) {
                        if (nicknameQuery.text.isEmpty()) {
                            Text(
                                text = stringResource(R.string.nickname_text_field_hint),
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
                    if (nicknameQuery.text.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = stringResource(R.string.keyboard_reset_button_description),
                            modifier = Modifier.clickable { onReset() }
                        )
                    }
                }
            },
            cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface)
        )
    }
}

@Composable
fun PasswordTextField(
    hint: String,
    passwordQuery: TextFieldState,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    var alpha by remember { mutableFloatStateOf(1f) }

    Row(
        modifier = modifier.border(0.1.dp, MaterialTheme.colorScheme.onSurface),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicSecureTextField(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(8.dp)
                )
                .onFocusChanged { alpha = if (it.isFocused) 0.6f else 1f }
                .padding(horizontal = 16.dp),
            state = passwordQuery,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
            decorator = { innerTextField ->
                Row(
                    modifier = Modifier.padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(10f)) {
                        if (passwordQuery.text.isEmpty()) {
                            Text(
                                text = hint,
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
                    if (passwordQuery.text.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = stringResource(R.string.keyboard_reset_button_description),
                            modifier = Modifier.clickable { onReset() }
                        )
                    }
                }
            },
            cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface)
        )
    }
}

@Composable
fun IdScreen(
    idQuery: TextFieldState,
    idIsAvailable: Boolean,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    val idComment = if (idIsAvailable || idQuery.text.isEmpty()) "" else "이메일 형식을 지켜주세요"
    Column(modifier = modifier) {
        Text(text = "이메일을 입력해주세요")
        Spacer(modifier = Modifier.height(16.dp))
        IdTextField(
            idQuery = idQuery,
            onReset = onReset
        )
        Text(text = idComment, style = MaterialTheme.typography.labelMedium, color = Color.Red)
    }
}

@Composable
fun NicknameScreen(
    nicknameQuery: TextFieldState,
    nickNameIsAvailable: Boolean,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    val nicknameComment =
        if (nickNameIsAvailable || nicknameQuery.text.isEmpty()) "" else "닉네임을 2글자 이상 입력해 주세요"
    Column(modifier = modifier) {
        Text(text = "닉네임을 입력해 주세요")
        Spacer(modifier = Modifier.height(16.dp))
        NicknameTextField(
            nicknameQuery = nicknameQuery,
            onReset = onReset
        )
        Text(
            text = nicknameComment,
            style = MaterialTheme.typography.labelMedium,
            color = Color.Red
        )
    }
}

@Composable
fun PasswordScreen(
    passwordQuery: TextFieldState,
    passwordCheckQuery: TextFieldState,
    passwordIsAvailable: Boolean,
    passwordIsSame: Boolean,
    onPasswordReset: () -> Unit,
    onPasswordCheckReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    val passwordComment =
        if (passwordIsAvailable || passwordQuery.text.isEmpty()) {
            stringResource(R.string.blank_text)
        } else {
            stringResource(R.string.password_available_text)
        }
    val passwordCheckComment =
        if (passwordIsSame || passwordCheckQuery.text.isEmpty()) {
            stringResource(R.string.blank_text)
        } else {
            stringResource(R.string.password_check_available_text)
        }
    Column(modifier = modifier) {
        Text(text = stringResource(R.string.type_password_guide_text))
        Spacer(modifier = Modifier.height(16.dp))
        PasswordTextField(
            hint = stringResource(R.string.password_text_field_hint),
            passwordQuery = passwordQuery,
            onReset = onPasswordReset
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = passwordComment,
            style = MaterialTheme.typography.labelMedium,
            color = Color.Red
        )
        Spacer(modifier = Modifier.height(16.dp))
        PasswordTextField(
            hint = stringResource(R.string.password_check_text_field_hint),
            passwordQuery = passwordCheckQuery,
            onReset = onPasswordCheckReset
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

@Preview
@Composable
fun SignInScreenPreview() {
    SignInScreen(onSignInComplete = {})
}
