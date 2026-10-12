package com.example.gurupoint.data.model

data class AgentCoinTransaction(
    val id: String,
    val agentId: String,
    val agentCode: String,
    val amount: Long,
    val type: TransactionType, // ADD, DEDUCT, etc.
    val targetCustomerMobile: String? = null,
    val reason: String,
    val dateTime: String,
    val balanceBefore: Long,
    val balanceAfter: Long
)
