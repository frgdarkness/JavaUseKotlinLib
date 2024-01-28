package com.zeroapp.bloodpresshelp.zeroimageeditor.ui.removebackground

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeroapp.bloodpresshelp.zeroimageeditor.data.remote.SDAIPhotoService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

class RemoveBackgroundResultViewModel : ViewModel() {

    private var outputImageBitmap: Bitmap? = null
    private val _removedBackgroundImageUrl = MutableLiveData<String>()
    val removedBackgroundImageUrl: LiveData<String> get() = _removedBackgroundImageUrl
    var downloadStatus: Boolean = false
    private val _isServerError = MutableLiveData<Boolean>()
    val isServerError: LiveData<Boolean> get() = _isServerError

    private val _clipImageOutput = MutableLiveData<ByteArray?>()
    val clipImageOutput: LiveData<ByteArray?> get() = _clipImageOutput

    private val _bitmapOutput = MutableLiveData<Bitmap>()
    val bitmapOutput: LiveData<Bitmap> get() = _bitmapOutput

    private var byteArrayOutput: ByteArray? = null

    private val sdAiPhotoService = SDAIPhotoService.create()

    fun setOutputImage(imageBitmap: Bitmap) {
        outputImageBitmap = imageBitmap
    }

    fun getOutputImage() = outputImageBitmap

    fun setDownloadImageStatus(isDownloaded: Boolean) {
        downloadStatus = isDownloaded
    }

    fun checkImageDownloaded() = downloadStatus

    fun removeBackground(imageUrl: String) {
        Log.d("asd123", "removeBackground: $imageUrl")
        if (imageUrl.isEmpty()) return
        viewModelScope.launch {
            val response = withTimeoutOrNull(90000) {
                withContext(Dispatchers.IO) {
                    sdAiPhotoService.removeBackgroundOkhttp(imageUrl)
                }
            }
            val newImageUrl = response?.output
            if (newImageUrl == null || newImageUrl.isEmpty()) {
                _isServerError.value = true
                Log.d("asd123", "removeBackgroundResult is null -> Timeout!")
                return@launch
            }
            _removedBackgroundImageUrl.value = response.output.firstOrNull()
        }
    }

    fun getByteArrayOutput() = byteArrayOutput
}