package com.example.gurupoint.data.model

data class BannerItem(
    val id: String,
    val title: String,
    val imageUrl: String,
    val targetLink: String? = null,
    val isActive: Boolean = true,
    val displayOrder: Int = 0
)
