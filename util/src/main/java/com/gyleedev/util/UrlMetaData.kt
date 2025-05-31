package com.gyleedev.util

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// 기본값 설정 안해주면 crash남

@Parcelize
data class UrlMetaData(
    var title: String = "",
    var name: String = "",
    var description: String = "",
    var url: String = "",
    var imageUrl: String = ""
) : Parcelable
