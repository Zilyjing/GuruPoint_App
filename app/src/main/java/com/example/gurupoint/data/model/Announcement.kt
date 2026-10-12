package com.example.gurupoint.data.model

data class Announcement(
    val id: String,
    val title: String,
    val message: String,
    val isActive: Boolean = true,
    val startDate: String = "",
    val endDate: String = ""
)
