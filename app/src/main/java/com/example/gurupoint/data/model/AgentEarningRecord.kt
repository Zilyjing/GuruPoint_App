package com.example.gurupoint.data.model

data class AgentEarningRecord(
    val id: String,
    val agentId: String,
    val agentCode: String,
    val customerMobile: String,
    val grossAdRevenue: Double,
    val agentShareAmount: Double,
    val masterAdminShareAmount: Double,
    val status: String = "APPROVED", // "PENDING", "APPROVED"
    val date: String
)
