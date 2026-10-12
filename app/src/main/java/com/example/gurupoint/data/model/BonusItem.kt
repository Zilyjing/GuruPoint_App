package com.example.gurupoint.data.model

data class BonusItem(
    val id: String,
    val name: String,
    val description: String,
    val bonusCoins: Long,
    val startDate: String,
    val endDate: String,
    val isActive: Boolean = true,
    val isClaimed: Boolean = false
)
