package com.zeroapp.bloodpresshelp.zeroimageeditor.data.remote.model

import com.zeroai.photoeditorai.data.remote.model.Meta
import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class SDImageResponse(
    val id: Long = -1,
    val generationTime: Double = 0.0,
    @SerialName("fetch_result")
    val fetchResult: String = "",
    val status: String,
    val meta: Meta,
    val output: List<String>,
//    val seed: Long
)

@kotlinx.serialization.Serializable
data class SDImageResponseTemp(
    val id: Long = -1,
    val generationTime: Double = 0.0,
    @SerialName("fetch_result")
    val fetchResult: String = "",
    val status: String,
    val meta: Meta,
    val output: String,
//    val seed: Long
)

fun SDImageResponseTemp.convertToSDImageResponse() = SDImageResponse(
    id,
    generationTime,
    fetchResult,
    status,
    meta,
    listOf(output)
)



enum class ImageResponseStatus(val value: String) {
    SUCCESS("success"),
    PROCESSING("processing")
}

fun String.getImageResponseStatus() = when (this) {
    ImageResponseStatus.SUCCESS.value -> ImageResponseStatus.SUCCESS
    else -> ImageResponseStatus.PROCESSING
}