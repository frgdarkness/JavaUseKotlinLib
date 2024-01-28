package com.zeroapp.bloodpresshelp.zeroimageeditor.data.remote.model

@kotlinx.serialization.Serializable
data class ClipBackgroundStyle(
    val id: Int,
    val imageUrl: String,
    val prompt: String
)
