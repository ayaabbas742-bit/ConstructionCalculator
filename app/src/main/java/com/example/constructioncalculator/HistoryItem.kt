package com.example.constructioncalculator

data class HistoryItem(
    val id: Int,
    val date: String,
    val element: String,
    val grade: String,
    val volume: Double,
    val cementBags: Int,
    val sandM3: Double,
    val gravelM3: Double,
    val steelKg: Double,
    val mixRatio: String
)