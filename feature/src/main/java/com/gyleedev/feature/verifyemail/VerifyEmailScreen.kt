package com.gyleedev.feature.verifyemail

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import com.gyleedev.domain.model.VerifiedState
import com.gyleedev.feature.R
import com.gyleedev.feature.component.TextField
import kotlinx.coroutines.flow.collectLatest

@RequiresApi(Build.VERSION_CODES.P)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifyEmailScreen(
    onSigninCancel: () -> Unit,
    onSigninComplete: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: VerifyEmailViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycle = LocalLifecycleOwner.current
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.uiEvent
            .flowWithLifecycle(lifecycle.lifecycle)
            .collectLatest {
                when (it) {
                    VerifyEmailUiEvent.Success -> {
                        snackbarHostState.showSnackbar(
                            message = context.getString(R.string.verify_email_success_message),
                            duration = SnackbarDuration.Short
                        )
                        onSigninComplete()
                    }
                    VerifyEmailUiEvent.Fail -> {
                        snackbarHostState.showSnackbar(
                            message = context.getString(R.string.verify_email_failure_message),
                            duration = SnackbarDuration.Short
                        )
                    }
                    VerifyEmailUiEvent.Cancel -> {
                        snackbarHostState.showSnackbar(
                            message = context.getString(R.string.verify_email_cancel_message),
                            duration = SnackbarDuration.Short
                        )
                        onSigninCancel()
                    }
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.verify_email_screen_title)) })
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        if (uiState.value is VerifyEmailUiState.Success) {
            val successState = uiState.value as VerifyEmailUiState.Success
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Spacer(modifier = Modifier.height(20.dp))
                Text(stringResource(R.string.verify_email_label))
                Spacer(modifier = Modifier.height(12.dp))
                TextField(
                    value = successState.userData.email,
                    onValueChange = {},
                    enabled = false
                )
                Spacer(modifier = Modifier.height(40.dp))
                if (successState.verifiedState == VerifiedState.NOTINPROGRESS) {
                    Button(
                        onClick = viewModel::verifyRequest,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.verify_email_button))
                    }
                }
                if (successState.verifiedState == VerifiedState.INPROGRESS) {
                    Button(
                        onClick = viewModel::checkVerified,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.verify_email_check_button))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = viewModel::cancelSignin,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors().copy(
                        containerColor = ButtonDefaults.buttonColors()
                            .copy().disabledContainerColor,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text(stringResource(R.string.verify_email_cancel_button))
                }
            }
        }
    }
}
