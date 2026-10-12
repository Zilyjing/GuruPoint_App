package com.example.gurupoint.data.model

enum class AdPlacement {
    HOMEPAGE_TOP,
    HOMEPAGE_BANNER,
    GAME_PAGE,
    RESULT_PAGE
}

data class AdItem(
    val id: String,
    val title: String,
    val imageUrl: String? = null,
    val adText: String,
    val targetUrl: String? = null,
    val placement: AdPlacement = AdPlacement.HOMEPAGE_BANNER,
    val isActive: Boolean = true
)
