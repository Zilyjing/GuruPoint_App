package com.example.gurupoint.data.model

data class VideoItem(
    val id: String,
    val title: String,
    val category: String,
    val thumbnailUrl: String? = null,
    val videoUrl: String,
    val description: String,
    val isActive: Boolean = true
)
