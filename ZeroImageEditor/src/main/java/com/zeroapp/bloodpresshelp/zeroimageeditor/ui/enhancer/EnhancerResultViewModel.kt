package com.zeroai.photoeditorai.ui.enhancer

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeroapp.bloodpresshelp.zeroimageeditor.data.remote.SDAIPhotoService
import com.zeroapp.bloodpresshelp.zeroimageeditor.data.remote.model.ImageType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

class EnhancerResultViewModel : ViewModel() {

    private var outputImageBitmap: Bitmap? = null
    private val _upscaledImageUrl = MutableLiveData<String>()
    val upscaledImageUrl: LiveData<String> get() = _upscaledImageUrl
    var downloadStatus: Boolean = false
    private val _isServerError = MutableLiveData<Boolean>()
    val isServerError: LiveData<Boolean> get() = _isServerError
    private var scaleValue: Int = 2
    private var imageType = ImageType.PHOTO

    private val sdAiPhotoService = SDAIPhotoService.create()


    fun setScale(value: Int) {
        scaleValue = value
    }

    fun getScale() = scaleValue

    fun setImageType(value: ImageType) {
        imageType = value
    }

    fun getImageType() = imageType


    fun setOutputImage(imageBitmap: Bitmap) {
        outputImageBitmap = imageBitmap
    }

    fun getOutputImage() = outputImageBitmap

    fun setDownloadImageStatus(isDownloaded: Boolean) {
        downloadStatus = isDownloaded
    }

    fun checkImageDownloaded() = downloadStatus

    fun upscaleImage(imageUrl: String, newScaleValue: Int) {
        Log.d("asd123", "upscaleImage: $imageUrl - scaleValue: $scaleValue")
        if (imageUrl.isEmpty()) return
        val finalScale = if (newScaleValue < scaleValue) newScaleValue else scaleValue
        viewModelScope.launch {
            val response = withTimeoutOrNull(90000) {
//                sdAiPhotoService.upscaleImage(imageUrl, finalScale, imageType)
                withContext(Dispatchers.IO) {
                    sdAiPhotoService.upscaleImageOkhttp(imageUrl, finalScale, imageType)
                }
            }
            Log.d("asd123", "upscaleImageResponse = $response")
            val newImageUrl = response?.output
            Log.d("asd123", "upscaleImage -> result = $newImageUrl")

            if (newImageUrl == null || newImageUrl.isEmpty()) {
                _isServerError.value = true
                Log.d("asd123", "newImage is null -> Timeout!")
                return@launch
            }
            _upscaledImageUrl.value = response.output
        }
    }
}