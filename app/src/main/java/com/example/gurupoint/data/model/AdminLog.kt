package com.example.gurupoint.data.model

data class AdminLog(
    val id: String,
    val adminMobile: String,
    val action: String,
    val targetType: String, // "GAME", "USER_COIN", "RESULT", "SETTINGS"
    val targetId: String,
    val details: String,
    val timestamp: String
)
