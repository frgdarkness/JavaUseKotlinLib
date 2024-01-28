package com.zeroapp.bloodpresshelp.zeroimageeditor.data.remote.model

@kotlinx.serialization.Serializable
data class SDUpdateScaleImageResponse(
    val status: String,
    val generationTime: Float = 0f,
    val id: Long = 0,
    val output: String
)