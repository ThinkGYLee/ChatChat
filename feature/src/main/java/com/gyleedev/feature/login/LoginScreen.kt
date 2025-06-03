package com.gyleedev.feature.login

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gyleedev.domain.model.LogInState
import com.gyleedev.feature.R
import com.gyleedev.feature.theme.ChatChatTheme

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLogInComplete: () -> Unit,
    onSignInClicked: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val idQuery by viewModel.idQuery.collectAsStateWithLifecycle()
    val passwordQuery by viewModel.passwordQuery.collectAsStateWithLifecycle()
    val idIsAvailable by viewModel.idIsAvailable.collectAsStateWithLifecycle()
    val passwordIsAvailable by viewModel.passwordIsAvailable.collectAsStateWithLifecycle()
    val signInIsAvailable by viewModel.logInIsAvailable.collectAsStateWithLifecycle()
    val idComment =
        if (idIsAvailable || idQuery.isEmpty()) "" else stringResource(R.string.id_incorrect_message)
    val passwordComment =
        if (passwordIsAvailable || passwordQuery.isEmpty()) "" else stringResource(R.string.password_incorrect_message)
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.fetchState.collect {
        }
    }

    LaunchedEffect(Unit) {
        viewModel.logInResult.collect {
            if (it is LogInState.Success) {
                Toast.makeText(
                    context,
                    context.getString(R.string.log_in_success_message),
                    Toast.LENGTH_SHORT
                ).show()
                onLogInComplete()
            } else {
                Toast.makeText(
                    context,
                    context.getString(R.string.log_in_failure_message),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.login_screen_top_bar_title)) }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column {
                Spacer(modifier = Modifier.height(20.dp))
                IdTextField(
                    idValue = idQuery,
                    onValueChange = { viewModel.editId(it) }
                )
                Text(
                    text = idComment,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Red
                )
                Spacer(modifier = Modifier.height(20.dp))
                PasswordTextField(
                    passwordValue = passwordQuery,
                    onValueChange = { viewModel.editPassword(it) }
                )
                Text(
                    text = passwordComment,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Red
                )
                Spacer(modifier = Modifier.height(40.dp))

                Button(
                    enabled = signInIsAvailable,
                    onClick = viewModel::logInButtonClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors().copy(
                        disabledContainerColor = ButtonDefaults.buttonColors().containerColor.copy(
                            alpha = 0.2f
                        ),
                        disabledContentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp)
                ) {
                    Text(
                        text = stringResource(R.string.login_screen_login_button_text),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onSignInClicked,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors().copy(
                        containerColor = ButtonDefaults.buttonColors().disabledContainerColor.copy(),
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp)
                ) {
                    Text(
                        text = stringResource(R.string.login_screen_sign_in_text),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Column {
                Text(
                    text = "Forgot your username or password?",
                    textDecoration = TextDecoration.Underline,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.clickable {}
                )
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun LoginBox(
    icon: Int,
    name: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .border(0.1.dp, Color.Black)
            .padding(20.dp)
            .clickable { onClick() },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = icon),
            contentDescription = stringResource(R.string.google_icon_description),
            modifier = Modifier.size(24.dp)
        )
        Text(text = stringResource(R.string.login_box_text, name))
        Spacer(modifier = Modifier.width(16.dp))
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
fun PasswordTextField(
    passwordValue: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = passwordValue,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = stringResource(R.string.password_text_field_hint),
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

@Preview
@Composable
fun IdTextField2Preview(
    modifier: Modifier = Modifier
) {
    var id by remember {
        mutableStateOf("")
    }
    ChatChatTheme {
        Column(
            modifier = Modifier
                .height(80.dp)
        ) {
            IdTextField(idValue = id, onValueChange = { id = it }, modifier = modifier)
        }
    }
}
