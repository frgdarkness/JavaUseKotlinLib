package com.zeroapp.bloodpresshelp.zeroimageeditor.data.remote.model

@kotlinx.serialization.Serializable
data class SDRemoveBackgroundResponse(
    val status: String,
    val id: Long,
    val output: List<String> = listOf()
)