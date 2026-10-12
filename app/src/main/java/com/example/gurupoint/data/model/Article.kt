package com.example.gurupoint.data.model

data class Article(
    val id: String,
    val title: String,
    val category: String,
    val thumbnailUrl: String? = null,
    val content: String,
    val author: String = "GuruPoint Team",
    val publishDate: String = "",
    val isActive: Boolean = true
)
