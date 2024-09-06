package com.gyleedev.chatchat.ui.login

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
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
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gyleedev.chatchat.R

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun LoginScreen(
    onSignInClicked: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val idQuery = rememberTextFieldState()
    val passwordQuery = rememberTextFieldState()

    LaunchedEffect(Unit) {
        viewModel.fetchState.collect {
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "로그인 하기")
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
            Row {
                Text(text = "소셜 로그인으로 가입할 수 있습니다.")
            }
            Spacer(modifier = Modifier.height(32.dp))
            IdTextField(searchQuery = idQuery, onReset = {
                idQuery.edit {
                    delete(
                        0,
                        idQuery.text.length
                    )
                }
            })
            PasswordTextField(searchQuery = passwordQuery, onReset = {
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
                Text(text = "로그인")
            }
            Spacer(modifier = Modifier.height(32.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Text(text = "아이디 찾기")
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "|")
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "비밀번호 찾기")
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "|")
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "회원가입",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        onSignInClicked()
                    }
                )
            }
            Spacer(modifier = Modifier.height(32.dp))

            HorizontalDivider()

            Spacer(modifier = Modifier.height(32.dp))

            LoginBox(icon = R.drawable.icons8__, name = "Google", onClick = {})

            Spacer(modifier = Modifier.height(24.dp))

            LoginBox(
                icon = R.drawable.icons8_facebook_96,
                name = "Facebook",
                onClick = { /*TODO*/ }
            )
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
            .clickable {
                onClick()
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = icon),
            contentDescription = "google icon",
            modifier = Modifier.size(24.dp)
        )
        Text(text = "$name 으로 시작하기")
        Spacer(modifier = Modifier.width(16.dp))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun IdTextField(
    searchQuery: TextFieldState,
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
            state = searchQuery,
            lineLimits = TextFieldLineLimits.SingleLine,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
            decorator = { innerTextField ->
                Row(
                    modifier = Modifier
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(10f)) {
                        if (searchQuery.text.isEmpty()) {
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
                    if (searchQuery.text.isNotEmpty()) {
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
    searchQuery: TextFieldState,
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
            state = searchQuery,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
            decorator = { innerTextField ->
                Row(
                    modifier = Modifier
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(10f)) {
                        if (searchQuery.text.isEmpty()) {
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
                    if (searchQuery.text.isNotEmpty()) {
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

@Preview
@Composable
fun LoginScreenPreview() {
    LoginScreen(onSignInClicked = {})
}
