package com.bogotov.prog_gifs.data.dto

import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.*

data class GifDto(
    val id: Int,
    val description: String,
    val votes: Int,
    val author: String,
    val date: Date,
    @SerializedName("gifURL") val gifUrl: String,
    val gifSize: String,
    @SerializedName("previewURL") val previewUrl: String,
    val width: Int,
    val height: Int,
    val commentsCount: Int,
    val fileSize: Int
) : Serializable
