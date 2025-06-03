package com.gyleedev.feature.signin

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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

    val idComment =
        if (idIsAvailable.value || idQuery.isEmpty()) "" else stringResource(R.string.email_format_error)

    val nickNameComment =
        if (nicknameIsAvailable.value || nicknameQuery.isEmpty()) "" else stringResource(R.string.nickname_length_error)

    val passwordComment =
        if (passwordIsAvailable.value || passwordQuery.isEmpty()) {
            stringResource(R.string.blank_text)
        } else {
            stringResource(R.string.password_available_text)
        }
    val passwordCheckComment =
        if (passwordIsSame.value || passwordCheckQuery.isEmpty()) {
            stringResource(R.string.blank_text)
        } else {
            stringResource(R.string.password_check_available_text)
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
                        style = MaterialTheme.typography.bodyLarge
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
                .padding(horizontal = 16.dp)
        ) {
            Text(text = stringResource(R.string.id_text_field_hint))
            Spacer(modifier = Modifier.height(16.dp))
            com.gyleedev.feature.component.TextField(
                value = idQuery,
                onValueChange = { viewModel.editId(it) },
                hint = stringResource(R.string.id_text_field_hint),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Email
                )
            )
            Text(text = idComment, style = MaterialTheme.typography.labelMedium, color = Color.Red)
            Spacer(modifier = Modifier.height(20.dp))
            Text(text = "Nickname")
            Spacer(modifier = Modifier.height(16.dp))
            com.gyleedev.feature.component.TextField(
                value = nicknameQuery,
                onValueChange = { viewModel.editNickname(it) },
                hint = stringResource(R.string.nickname_text_field_hint),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                )
            )
            Text(
                text = nickNameComment,
                style = MaterialTheme.typography.labelMedium,
                color = Color.Red
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(text = stringResource(R.string.password_text_field_hint))
            Spacer(modifier = Modifier.height(16.dp))
            com.gyleedev.feature.component.TextField(
                value = passwordQuery,
                onValueChange = { viewModel.editPassword(it) },
                hint = stringResource(R.string.password_text_field_hint),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Password
                ),
                visualTransformation = PasswordVisualTransformation()
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
            com.gyleedev.feature.component.TextField(
                value = passwordCheckQuery,
                onValueChange = { viewModel.editPasswordCheck(it) },
                hint = stringResource(R.string.password_check_text_field_hint),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done,
                    keyboardType = KeyboardType.Password
                ),
                visualTransformation = PasswordVisualTransformation()
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
