package com.gyleedev.chatchat.ui.signin

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.text2.BasicSecureTextField
import androidx.compose.foundation.text2.BasicTextField2
import androidx.compose.foundation.text2.input.TextFieldLineLimits
import androidx.compose.foundation.text2.input.TextFieldState
import androidx.compose.foundation.text2.input.delete
import androidx.compose.foundation.text2.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SignInScreen(
    modifier: Modifier = Modifier,
    viewModel: SignInViewModel = hiltViewModel()
) {
    val idQuery = rememberTextFieldState()
    val passwordQuery = rememberTextFieldState()
    val passwordCheckQuery = rememberTextFieldState()

    LaunchedEffect(Unit) {
        viewModel.fetchState.collect {
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "회원 가입")
                }
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
            IdScreen(idQuery = idQuery, onReset = {
                idQuery.edit {
                    delete(
                        0,
                        idQuery.text.length
                    )
                }
            })
            Spacer(modifier = Modifier.height(32.dp))

            PasswordTextField(passwordQuery = passwordQuery, onReset = {
                passwordQuery.edit {
                    delete(
                        0,
                        passwordQuery.text.length
                    )
                }
            })
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    viewModel.logInButtonClick(
                        idQuery.text.toString(),
                        passwordQuery.text.toString()
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "회원 가입하기")
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun IdTextField(
    idQuery: TextFieldState,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    var alpha by remember { mutableFloatStateOf(1f) }

    Row(
        modifier = modifier.border(0.1.dp, Color.Black),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicTextField2(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(8.dp)
                )
                .onFocusChanged {
                    alpha = if (it.isFocused) 0.6f else 1f
                }
                .padding(horizontal = 16.dp),
            state = idQuery,
            lineLimits = TextFieldLineLimits.SingleLine,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
            decorator = { innerTextField ->
                Row(
                    modifier = Modifier
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(10f)) {
                        if (idQuery.text.isEmpty()) {
                            Text(
                                text = "아이디",
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
                            contentDescription = null,
                            modifier = Modifier.clickable { onReset() }
                        )
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PasswordTextField(
    passwordQuery: TextFieldState,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    var alpha by remember { mutableFloatStateOf(1f) }

    Row(
        modifier = modifier.border(0.1.dp, Color.Black),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicSecureTextField(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(8.dp)
                )
                .onFocusChanged {
                    alpha = if (it.isFocused) 0.6f else 1f
                }
                .padding(horizontal = 16.dp),
            state = passwordQuery,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
            decorator = { innerTextField ->
                Row(
                    modifier = Modifier
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(10f)) {
                        if (passwordQuery.text.isEmpty()) {
                            Text(
                                text = "비밀번호",
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
                            contentDescription = null,
                            modifier = Modifier.clickable { onReset() }
                        )
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun IdScreen(idQuery: TextFieldState, onReset: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(text = "이메일을 입력해주세요")
        Spacer(modifier = Modifier.height(36.dp))
        IdTextField(idQuery = idQuery, onReset = {
            idQuery.edit {
                delete(
                    0,
                    idQuery.text.length
                )
            }
        })
        Spacer(modifier = Modifier.height(36.dp))
        Button(
            onClick = {

            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "다음")
        }

    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PasswordScreen(
    passwordQuery: TextFieldState,
    passwordCheckQuery: TextFieldState,
    onPasswordReset: () -> Unit,
    onPasswordCheckReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    val passwordValid = remember {
        mutableStateOf(
            false
        )
    }

    Column(modifier = modifier) {
        Text(text = "비밀번호를 입력해주세요")
        Spacer(modifier = Modifier.height(36.dp))
        PasswordTextField(passwordQuery = passwordQuery, onReset = {
            onPasswordReset()
        })
        Spacer(modifier = Modifier.height(36.dp))
        PasswordTextField(passwordQuery = passwordCheckQuery, onReset = {
            onPasswordCheckReset()
        })

        Button(
            onClick = {

            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "다음")
        }

    }
}


@Preview
@Composable
fun SignInScreenPreview() {
    SignInScreen()
}
