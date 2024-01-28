package com.zeroai.photoeditorai.data.remote.model

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class Meta(
    @SerialName("H")
    val h: Int,
    @SerialName("W")
    val w: Int,
    val steps: Int,

//    @SerialName("enable_attention_slicing")
//    val enableAttentionSlicing: String,

    @SerialName("guidance_scale")
    val guidanceScale: Double,
    @SerialName("file_prefix")
    val filePrefix: String,
//    val model: String,
    @SerialName("n_samples")
    val nSamples: Int,
    @SerialName("negative_prompt")
    val negativePrompt: String,
//    val outdir: String,
    val prompt: String,
//    val revision: String,
//    @SerialName("safetychecker")
//    val safetyChecker: String,
    val seed: Long,
//    val vae: String
)