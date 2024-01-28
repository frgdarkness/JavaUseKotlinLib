package com.zeroapp.bloodpresshelp.zeroimageeditor.data.remote.model

@kotlinx.serialization.Serializable
data class SDLoraData(
    val id: Int,
    val vipType: Int = 0,
    val loraId: String,
    val loraName: String,
    val sampleUrl: List<String>,
    val urlPrefer: String
)
