package com.example.gurupoint.data.model

enum class TransactionType {
    ADD,
    DEDUCT,
    REWARD,
    REFERRAL,
    BONUS,
    GAME_SELECTION
}

data class CoinTransaction(
    val id: String,
    val userId: String,
    val amount: Long,
    val type: TransactionType,
    val reason: String,
    val dateTime: String,
    val balanceBefore: Long,
    val balanceAfter: Long,
    val performedByAdminId: String? = null
)
