package com.zeroapp.bloodpresshelp.zeroimageeditor.data.remote.model

@kotlinx.serialization.Serializable
data class AIWallpaperData(
    val id: Int,
    val vipType: Int = 0,
    val url: String
)
