package com.example.constructioncalculator

data class HistoryItem(
    val type: String,
    val height: Double,
    val steps: Int,
    val length: Double,
    val area: Double,
    val date: Long
)