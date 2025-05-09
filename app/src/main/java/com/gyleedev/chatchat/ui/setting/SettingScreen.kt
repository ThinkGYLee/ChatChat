package com.gyleedev.chatchat.ui.setting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gyleedev.chatchat.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    onLogoutRequest: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingViewModel = hiltViewModel()
) {
    var openDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            Button(onClick = { openDialog = true }) {
                Text(text = stringResource(R.string.logout_button_text))
            }
        }
        if (openDialog) {
            BasicAlertDialog(
                onDismissRequest = { openDialog = false },
                content = {
                    Surface(
                        modifier = Modifier.wrapContentSize(),
                        shape = MaterialTheme.shapes.large,
                        tonalElevation = AlertDialogDefaults.TonalElevation
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Text(
                                text = stringResource(R.string.logout_dialog_message),
                                style = MaterialTheme.typography.titleLarge
                            )
                            Spacer(Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Absolute.Right
                            ) {
                                TextButton(
                                    onClick = { openDialog = false }
                                ) {
                                    Text(text = stringResource(R.string.dialog_dismiss_button_text))
                                }
                                TextButton(
                                    onClick = {
                                        openDialog = false
                                        viewModel.logout()
                                        onLogoutRequest()
                                    }
                                ) {
                                    Text(text = stringResource(R.string.dialog_confirm_button_text))
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.wrapContentSize()
            )
        }
    }
}
