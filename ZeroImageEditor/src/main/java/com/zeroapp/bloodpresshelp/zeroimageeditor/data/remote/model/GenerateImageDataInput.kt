package com.zeroapp.bloodpresshelp.zeroimageeditor.data.remote.model

@kotlinx.serialization.Serializable
data class GenerateImageDataInput(
    var prompt: String = "",
    var styleId: Int = 28,
    var width: Int = 512,
    var height: Int = 512,
    var negativePrompt: String = "",
    var strength: Int = 100,
    var mode: String = "depth",
    var cfg: Float = 0.5f
)