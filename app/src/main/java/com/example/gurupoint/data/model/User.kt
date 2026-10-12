package com.example.gurupoint.data.model

data class User(
    val id: String,
    val name: String,
    val mobileNumber: String,
    val profilePhotoUrl: String? = null,
    val coinBalance: Long = 0L,
    val referralCode: String = "",
    val referredBy: String? = null,
    val assignedAgentCode: String? = null,
    val agentWhatsApp: String? = null,
    val agentTelegram: String? = null,
    val isActive: Boolean = true,
    val role: String = "USER", // "USER", "AGENT", "ADMIN", "SUPER_ADMIN"
    val createdAt: String = ""
)
