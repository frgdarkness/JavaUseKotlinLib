package com.zeroapp.bloodpresshelp.zeroimageeditor.data.remote.model

@kotlinx.serialization.Serializable
data class SDFetchResponse(
    val status: String,
    val id: Long = 0,
    val message: String = "",
    val output: List<String> = listOf()
)
