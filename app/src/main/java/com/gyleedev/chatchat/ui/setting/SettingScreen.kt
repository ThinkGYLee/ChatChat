package com.gyleedev.chatchat.ui.setting

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gyleedev.chatchat.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    onLogoutRequest: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingViewModel = hiltViewModel()
) {
    var openDialog by remember { mutableStateOf(false) }
    val items by viewModel.items.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text(text = stringResource(R.string.setting_text)) }) }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {
            items.forEach {
                if (it is SettingItems.Header) {
                    SettingHeader(it.title)
                } else if (it is SettingItems.Item) {
                    SettingItem(it)
                }
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

@Composable
fun SettingHeader(
    title: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(color = MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(start = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(title),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.ExtraLight,
            modifier = Modifier.alpha(0.4f)
        )
    }
}

@Composable
fun SettingItem(
    settingItems: SettingItems.Item,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .clickable {}
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row {
            Icon(imageVector = settingItems.icon, contentDescription = null)
            Spacer(modifier = Modifier.width(20.dp))
            Text(
                text = stringResource(settingItems.title),
                style = MaterialTheme.typography.titleMedium
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = LocalContentColor.current.copy(alpha = 0.6f)
        )
    }
}
