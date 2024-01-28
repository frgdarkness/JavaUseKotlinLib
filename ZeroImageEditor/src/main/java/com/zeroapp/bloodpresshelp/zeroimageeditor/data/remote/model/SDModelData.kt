package com.zeroapp.bloodpresshelp.zeroimageeditor.data.remote.model

@kotlinx.serialization.Serializable
data class SDModelData(
    val id: Int,
    val modelId: String,
    val modelName: String,
    val thumb: String = "",
    val sampleUrl: List<String>,
    val urlPrefer: String,
    val vipType: Int = 0,
)
