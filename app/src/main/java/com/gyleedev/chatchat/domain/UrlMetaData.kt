package com.gyleedev.chatchat.domain

// 기본값 설정 안해주면 crash남
data class UrlMetaData(
    var title: String? = null,
    var name: String? = null,
    var description: String? = null,
    var url: String? = null,
    var imageUrl: String? = null,
)
