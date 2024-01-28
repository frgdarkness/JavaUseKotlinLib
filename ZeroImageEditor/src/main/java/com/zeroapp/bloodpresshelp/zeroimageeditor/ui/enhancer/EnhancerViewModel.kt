package com.zeroai.photoeditorai.ui.enhancer

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.zeroapp.bloodpresshelp.zeroimageeditor.data.remote.model.ImageType

class EnhancerViewModel : ViewModel() {
    private var imageUri: Uri? = null
    private var scaleValue: Int = 2
    private var imageType = ImageType.PHOTO

    fun setScale(value: Int) {
        scaleValue = value
    }

    fun getScale() = scaleValue

    fun setImageType(value: ImageType) {
        imageType = value
    }

    fun getImageType() = imageType

    fun setImageUri(value: Uri) {
        imageUri = value
    }

    fun getImageUri() = imageUri
}