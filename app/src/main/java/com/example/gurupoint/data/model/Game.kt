package com.example.gurupoint.data.model

data class Game(
    val id: String,
    val name: String,
    val description: String,
    val iconUrl: String? = null,
    val openTime: String, // e.g., "10:00 AM"
    val closeTime: String, // e.g., "04:00 PM"
    val resultTime: String, // e.g., "05:00 PM"
    val isOpen: Boolean = true,
    val isActive: Boolean = true,
    val isHidden: Boolean = false,
    val displayOrder: Int = 0,
    val minCoin: Long = 10L,
    val maxCoin: Long = 10000L,
    val latestResult: String? = null
)
