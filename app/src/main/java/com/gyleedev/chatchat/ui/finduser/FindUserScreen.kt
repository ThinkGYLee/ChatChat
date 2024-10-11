package com.gyleedev.chatchat.ui.finduser

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.delete
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gyleedev.chatchat.R
import com.skydoves.landscapist.glide.GlideImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FindUserScreen(
    onFindComplete: () -> Unit,
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

    LaunchedEffect(Unit) {
        viewModel.searchFailure.collect {
            Toast.makeText(
                context,
                context.getString(R.string.search_user_failure_message),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    Scaffold(modifier = modifier, topBar = {
        TopAppBar(
            title = {
                Text(
                    text = "연락처로 추가",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(
                    onClick = { /*TODO*/ }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "ArrowBack Button"
                    )
                }
            },
            actions = {
                TextButton(
                    onClick = viewModel::fetchUserData,
                    enabled = emailIsAvailable.value
                ) {
                    Text(text = "확인")
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
            FindUserTextField(idQuery = emailQuery, onReset = {
                emailQuery.edit {
                    delete(
                        0,
                        emailQuery.text.length
                    )
                }
            })

            if (userData.value != null) {
                FindUserCard(onFindComplete = { onFindComplete() })
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
                    .onFocusChanged {
                        alpha = if (it.isFocused) 0.6f else 1f
                    }
                    .padding(horizontal = 16.dp),
                state = idQuery,
                lineLimits = TextFieldLineLimits.SingleLine,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email),
                decorator = { innerTextField ->
                    Row(
                        modifier = Modifier
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(10f)) {
                            if (idQuery.text.isEmpty()) {
                                Text(
                                    text = "친구 이메일 주소",
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
            Spacer(modifier = Modifier.height(4.dp))
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        }
    }
}

@Composable
fun FindUserCard(onFindComplete: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .padding(20.dp)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GlideImage(
                imageModel = { R.drawable.icons8__ }
            )
            Text(text = "aaaa")
            TextButton(onClick = { onFindComplete() }) {
                Text(text = "친구 추가")
            }
        }
    }
}

@Composable
@Preview
fun FindUserScreenPreview() {
    MaterialTheme {
        FindUserScreen(onFindComplete = {})
    }
}
