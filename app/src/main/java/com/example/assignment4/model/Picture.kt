package com.example.assignment4.model

import kotlinx.serialization.Serializable

@Serializable
data class Picture (
    val id: String,
    val author: String,
    val width: Int,
    val height: Int,
    val url: String,
    val download_url: String
)