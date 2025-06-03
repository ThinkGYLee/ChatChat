package com.gyleedev.feature.signin

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gyleedev.domain.model.SignInResult
import com.gyleedev.feature.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignInScreen(
    onSignInComplete: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SignInViewModel = hiltViewModel()
) {

    val idQuery by viewModel.idQuery.collectAsStateWithLifecycle()
    val nicknameQuery by viewModel.nicknameQuery.collectAsStateWithLifecycle()
    val passwordQuery by viewModel.passwordQuery.collectAsStateWithLifecycle()
    val passwordCheckQuery by viewModel.passwordCheckQuery.collectAsStateWithLifecycle()
    val signInIsAvailable = viewModel.signInIsAvailable.collectAsStateWithLifecycle()
    val idIsAvailable = viewModel.idIsAvailable.collectAsStateWithLifecycle()
    val nicknameIsAvailable = viewModel.nicknameIsAvailable.collectAsStateWithLifecycle()
    val passwordIsAvailable = viewModel.passwordIsAvailable.collectAsStateWithLifecycle()
    val passwordIsSame = viewModel.passwordIsSame.collectAsStateWithLifecycle()
    val context = LocalContext.current


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
        modifier = modifier,
        bottomBar = {
            Column(
                modifier = Modifier
                    .navigationBarsPadding()
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)

            ) {
                Button(
                    enabled = signInIsAvailable.value,
                    onClick = viewModel::signInRequest,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.signin_button_title),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                IdArea(
                    idQuery = idQuery,
                    idIsAvailable = idIsAvailable.value,
                    onReset = {},
                    onIdChange = {
                        viewModel.editId(it)
                    }
                )
                Spacer(modifier = Modifier.height(20.dp))

                NickNameArea(
                    nicknameQuery = nicknameQuery,
                    nickNameIsAvailable = nicknameIsAvailable.value,
                    onReset = {},
                    onNicknameChanged = {
                        viewModel.editNickname(it)
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))

                PasswordArea(
                    passwordQuery = passwordQuery,
                    passwordCheckQuery = passwordCheckQuery,
                    passwordIsAvailable = passwordIsAvailable.value,
                    passwordIsSame = passwordIsSame.value,
                    onPasswordReset = {},
                    onPasswordCheckReset = {},
                    onPasswordChange = {
                        viewModel.editPassword(it)
                    },
                    onPasswordCheckChange = {
                        viewModel.editPasswordCheck(it)
                    }
                )
            }
        }
    }
}

@Composable
fun IdTextField(
    idValue: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = idValue,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = stringResource(R.string.id_text_field_hint),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        },
        shape = RoundedCornerShape(20.dp),
        colors = TextFieldDefaults.colors().copy(
            focusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Next,
            keyboardType = KeyboardType.Email
        ),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun NicknameTextField(
    nicknameValue: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = nicknameValue,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = "Nickname",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        },
        shape = RoundedCornerShape(20.dp),
        colors = TextFieldDefaults.colors().copy(
            focusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Next
        ),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun PasswordTextField(
    hint: String,
    passwordValue: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = passwordValue,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = hint,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        },
        visualTransformation = PasswordVisualTransformation(),
        shape = RoundedCornerShape(20.dp),
        colors = TextFieldDefaults.colors().copy(
            focusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done,
            keyboardType = KeyboardType.Password
        ),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun IdArea(
    idQuery: String,
    onIdChange: (String) -> Unit,
    idIsAvailable: Boolean,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    val idComment =
        if (idIsAvailable || idQuery.isEmpty()) "" else stringResource(R.string.email_format_error)
    Column(modifier = modifier) {
        Text(text = stringResource(R.string.id_text_field_hint))
        Spacer(modifier = Modifier.height(16.dp))
        IdTextField(
            idValue = idQuery,
            onValueChange = onIdChange
        )
        Text(text = idComment, style = MaterialTheme.typography.labelMedium, color = Color.Red)
    }
}

@Composable
fun NickNameArea(
    nicknameQuery: String,
    onNicknameChanged: (String) -> Unit,
    nickNameIsAvailable: Boolean,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    val nickNameComment =
        if (nickNameIsAvailable || nicknameQuery.isEmpty()) "" else stringResource(R.string.nickname_length_error)
    Column(modifier = modifier) {
        Text(text = "Nickname")
        Spacer(modifier = Modifier.height(16.dp))
        NicknameTextField(
            nicknameValue = nicknameQuery,
            onValueChange = onNicknameChanged
        )
        Text(
            text = nickNameComment,
            style = MaterialTheme.typography.labelMedium,
            color = Color.Red
        )
    }
}

@Preview
@Composable
fun Test() {
    NickNameArea("", {}, true, {})
}

@Composable
fun PasswordArea(
    passwordQuery: String,
    onPasswordChange: (String) -> Unit,
    passwordCheckQuery: String,
    onPasswordCheckChange: (String) -> Unit,
    passwordIsAvailable: Boolean,
    passwordIsSame: Boolean,
    onPasswordReset: () -> Unit,
    onPasswordCheckReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    val passwordComment =
        if (passwordIsAvailable || passwordQuery.isEmpty()) {
            stringResource(R.string.blank_text)
        } else {
            stringResource(R.string.password_available_text)
        }
    val passwordCheckComment =
        if (passwordIsSame || passwordCheckQuery.isEmpty()) {
            stringResource(R.string.blank_text)
        } else {
            stringResource(R.string.password_check_available_text)
        }
    Column(modifier = modifier) {
        Text(text = stringResource(R.string.password_text_field_hint))
        Spacer(modifier = Modifier.height(16.dp))
        PasswordTextField(
            hint = stringResource(R.string.password_text_field_hint),
            passwordValue = passwordQuery,
            onValueChange = onPasswordChange
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = passwordComment,
            style = MaterialTheme.typography.labelMedium,
            color = Color.Red
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Confirm Password")
        Spacer(modifier = Modifier.height(16.dp))
        PasswordTextField(
            hint = stringResource(R.string.password_check_text_field_hint),
            passwordValue = passwordCheckQuery,
            onValueChange = onPasswordCheckChange
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

