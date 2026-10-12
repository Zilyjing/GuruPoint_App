package com.example.gurupoint.data.model

data class RegisteredAccount(
    val id: String,
    val name: String,
    val mobileNumber: String,
    val passwordHash: String,
    val coinBalance: Long = 500L,
    val referralCode: String = "",
    val referredBy: String? = null,
    val assignedAgentCode: String? = null,
    val agentWhatsApp: String? = null,
    val agentTelegram: String? = null,
    val role: String = "USER",
    val status: String = "ACTIVE",
    val activeDeviceId: String? = null,
    val createdAt: String = ""
)
