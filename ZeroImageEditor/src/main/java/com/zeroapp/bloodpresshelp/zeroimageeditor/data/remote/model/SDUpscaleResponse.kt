package com.zeroapp.bloodpresshelp.zeroimageeditor.data.remote.model

@kotlinx.serialization.Serializable
data class SDUpscaleResponse(
    val status: String,
    val generationTime: Double = 0.0,
    val id: Long,
    val output: String = ""
)