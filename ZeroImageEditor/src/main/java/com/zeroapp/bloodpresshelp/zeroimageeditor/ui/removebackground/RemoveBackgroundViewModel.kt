package com.zeroapp.bloodpresshelp.zeroimageeditor.ui.removebackground

import android.net.Uri
import androidx.lifecycle.ViewModel

class RemoveBackgroundViewModel : ViewModel() {

    private var imageUri: Uri? = null

    fun setImageUri(value: Uri) {
        imageUri = value
    }

    fun getImageUri() = imageUri
}