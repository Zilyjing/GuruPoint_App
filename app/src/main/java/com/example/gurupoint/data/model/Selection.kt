package com.example.gurupoint.data.model

enum class SelectionMode {
    MODE_A, // 00-99 / 1-100
    MODE_B  // 0-9 / 1-10
}

enum class SelectionStatus {
    PENDING,
    WON,
    LOST
}

data class Selection(
    val id: String,
    val userId: String,
    val gameId: String,
    val gameName: String,
    val mode: SelectionMode,
    val selectedNumber: String,
    val coinQuantity: Long,
    val potentialReward: Long,
    val status: SelectionStatus = SelectionStatus.PENDING,
    val createdAt: String
)
