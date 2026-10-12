package com.example.gurupoint.data.model

enum class AgentStatus {
    ACTIVE,
    INACTIVE,
    BLOCKED,
    SUSPENDED
}

data class AgentPermissions(
    val canViewCustomers: Boolean = true,
    val canAddCustomerCoins: Boolean = true,
    val canDeductCustomerCoins: Boolean = true,
    val canViewTransactions: Boolean = true,
    val canViewGameHistory: Boolean = true,
    val canViewResults: Boolean = true
)

data class Agent(
    val id: String,
    val name: String,
    val mobileNumber: String,
    val passwordHash: String,
    val agentCode: String, // Unique e.g. "AG1001"
    val status: AgentStatus = AgentStatus.ACTIVE,
    val coinBalance: Long = 0L,
    val totalCustomers: Int = 0,
    val todayEarnings: Double = 0.0,
    val totalEarnings: Double = 0.0,
    val permissions: AgentPermissions = AgentPermissions(),
    val createdAt: String = ""
)
