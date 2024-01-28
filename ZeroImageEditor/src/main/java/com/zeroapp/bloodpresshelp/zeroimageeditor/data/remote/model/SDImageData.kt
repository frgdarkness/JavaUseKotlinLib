package com.zeroapp.bloodpresshelp.zeroimageeditor.data.remote.model

import android.graphics.Bitmap

data class SDImageData(
    var sdResponseData: SDImageResponse,
    var imageUrl: String,
    var seed: Long = 0,
    var bitmap: Bitmap? = null,
    var isScaled: Boolean = false,
    var isSaved: Boolean = false,
    var isRemovedWaterMark: Boolean = false
)
