package com.gyleedev.chatchat.ui.setting.changetheme

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gyleedev.chatchat.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangeThemeScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    changeThemeViewModel: ChangeThemeViewModel = hiltViewModel()
) {
    val currentTheme by changeThemeViewModel.currentTheme.collectAsStateWithLifecycle()

    val themeList = listOf(
        ThemeOption(R.string.theme_system, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM),
        ThemeOption(R.string.theme_light, AppCompatDelegate.MODE_NIGHT_NO),
        ThemeOption(R.string.theme_dark, AppCompatDelegate.MODE_NIGHT_YES)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.setting_theme_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.navigation_arrow_back_icon_description)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            themeList.forEach { theme ->
                ThemeItem(
                    themeName = stringResource(id = theme.nameRes),
                    isSelected = currentTheme == theme.mode,
                    onClick = {
                        AppCompatDelegate.setDefaultNightMode(theme.mode)
                        changeThemeViewModel.setTheme(theme.mode)
                    }
                )
            }
        }
    }
}

@Composable
fun ThemeItem(
    themeName: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = themeName,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
    Spacer(modifier = Modifier.height(1.dp))
}

data class ThemeOption(
    val nameRes: Int,
    val mode: Int
)
