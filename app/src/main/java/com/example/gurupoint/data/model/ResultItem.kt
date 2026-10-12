package com.example.gurupoint.data.model

data class ResultItem(
    val id: String,
    val gameId: String,
    val gameName: String,
    val resultDate: String,
    val resultTime: String,
    val declaredNumber: String?, // Null if awaiting update
    val isDeclared: Boolean = false,
    val sourceName: String? = null
)
