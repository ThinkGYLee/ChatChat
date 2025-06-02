package com.gyleedev.feature.setting

import androidx.compose.ui.graphics.vector.ImageVector

sealed interface SettingItems {

    data class Header(
        val title: Int
    ) : SettingItems

    data class Item(
        val title: Int,
        val icon: ImageVector,
        val event: SettingEvent
    ) : SettingItems
}
