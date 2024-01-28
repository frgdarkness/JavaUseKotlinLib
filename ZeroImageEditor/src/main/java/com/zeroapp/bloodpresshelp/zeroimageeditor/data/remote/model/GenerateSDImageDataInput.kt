package com.zeroapp.bloodpresshelp.zeroimageeditor.data.remote.model

@kotlinx.serialization.Serializable
data class GenerateSDImageDataInput(
    var prompt: String,
    val modelId: String = "",
    var negativePrompt: String = "",
    val width: Int = 512,
    val height: Int = 512,
    val samples: Int = 1,
    var numInferenceSteps: Int = 20,
    val safetyChecker: Boolean = false,
    val enhancePrompt: Boolean = true,
    var seed: String = "",
    var guidanceScale: Float = 7.5f,
    val multiLingual: Boolean = false,
    val panorama: Boolean = false,
    val selfAttention: Boolean = false,
    val upscale: Boolean = false,
    val embeddingsModel: String = "",
    val webHook: String = "",
    val trackId: Long? = null,
    val loraModel: String = "",
    val loraStrength: Float = 0f,
    var scheduler: String = "EulerAncestralDiscreteScheduler",
    val algorithmType: String = "",
)
