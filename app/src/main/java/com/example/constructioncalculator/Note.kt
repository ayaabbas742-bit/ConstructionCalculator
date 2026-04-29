package com.example.constructioncalculator

import java.io.Serializable

data class Note(
    val id: Long = 0,
    var title: String = "",
    var subtitle: String = "",
    var body: String = "",
    var color: Int = android.graphics.Color.BLACK,
    var date: String = "",
    var images: String = "",
    var files: String = "",
    var links: String = ""
) : Serializable