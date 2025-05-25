package com.gyleedev.chatchat.ui.setting.changelanguage

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import com.gyleedev.chatchat.R
import java.util.Locale

val languageList = listOf(
    R.string.language_default,
    R.string.language_arabic,
    R.string.language_chinese,
    R.string.language_english,
    R.string.language_french,
    R.string.language_german,
    R.string.language_indonesian,
    R.string.language_italian,
    R.string.language_japanese,
    R.string.language_korean,
    R.string.language_persian,
    R.string.language_portuguese,
    R.string.language_spanish,
    R.string.language_turkish,
    R.string.language_vietnamese
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangeLanguageScreen(
    onBackPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedLanguage by remember {
        mutableStateOf(getCurrentLanguage(context))
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.setting_text)) },
                navigationIcon = {
                    IconButton(onClick = onBackPress) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.navigation_arrow_back_icon_description)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            languageList.forEach { stringResId ->
                LanguageItem(
                    languageResId = stringResId,
                    isSelected = selectedLanguage == getLanguageCode(stringResId),
                    onLanguageClick = { languageCode ->
                        selectedLanguage = languageCode
                        setAppLanguage(languageCode)
                    }
                )
            }
        }
    }
}

@Composable
fun LanguageItem(
    languageResId: Int,
    isSelected: Boolean,
    onLanguageClick: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onLanguageClick(getLanguageCode(languageResId)) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(languageResId),
            modifier = Modifier.weight(1f)
        )
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null
            )
        }
    }
}

private fun getCurrentLanguage(context: Context): String {
    val appLocale = AppCompatDelegate.getApplicationLocales()
    return if (appLocale.isEmpty) {
        context.resources.configuration.locales[0].language
    } else {
        appLocale[0]?.language ?: "default"
    }
}

private fun setAppLanguage(languageCode: String) {
    val localeList = if (languageCode == "default") {
        LocaleListCompat.getEmptyLocaleList()
    } else {
        LocaleListCompat.create(Locale(languageCode))
    }
    AppCompatDelegate.setApplicationLocales(localeList)
}

private fun getLanguageCode(stringResId: Int): String {
    return when (stringResId) {
        R.string.language_default -> "default"
        R.string.language_arabic -> "ar"
        R.string.language_chinese -> "zh"
        R.string.language_english -> "en"
        R.string.language_french -> "fr"
        R.string.language_german -> "de"
        R.string.language_indonesian -> "id"
        R.string.language_italian -> "it"
        R.string.language_japanese -> "ja"
        R.string.language_korean -> "ko"
        R.string.language_persian -> "fa"
        R.string.language_portuguese -> "pt"
        R.string.language_spanish -> "es"
        R.string.language_turkish -> "tr"
        R.string.language_vietnamese -> "vi"
        else -> "default"
    }
}
