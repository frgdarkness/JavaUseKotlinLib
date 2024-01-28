package com.zeroapp.bloodpresshelp.zeroimageeditor.data.remote

import android.graphics.Bitmap
import com.zeroapp.bloodpresshelp.zeroimageeditor.data.remote.model.GenerateImageToSDImageDataInput
import com.zeroapp.bloodpresshelp.zeroimageeditor.data.remote.model.GenerateSDImageDataInput
import com.zeroapp.bloodpresshelp.zeroimageeditor.data.remote.model.SDImageResponse
import com.zeroapp.bloodpresshelp.zeroimageeditor.data.remote.model.SDRemoveBackgroundResponse
import com.zeroapp.bloodpresshelp.zeroimageeditor.data.remote.model.SDUpscaleResponse
import com.zeroapp.bloodpresshelp.zeroimageeditor.data.remote.model.ImageType
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Job

interface SDAIPhotoService {

    suspend fun textToImage(input: GenerateSDImageDataInput? = null): SDImageResponse?

    suspend fun textToImageWithModel(input: GenerateSDImageDataInput? = null): SDImageResponse?

    suspend fun upscaleImage(imageUrl: String, scale: Int, imageType: ImageType = ImageType.PHOTO): SDUpscaleResponse?

    suspend fun upscaleImageOkhttp(imageUrl: String, scale: Int, imageType: ImageType = ImageType.PHOTO): SDUpscaleResponse?

    suspend fun upscaleImageOld(imageUrl: String, scale: Int): SDUpscaleResponse?

    suspend fun removeBackground(imageUrl: String): SDRemoveBackgroundResponse?
    suspend fun removeBackgroundOkhttp(imageUrl: String): SDRemoveBackgroundResponse?

    suspend fun fetchGenerateImageRequest(requestId: Long): String?

    suspend fun imageToImage(input: GenerateImageToSDImageDataInput? = null): SDImageResponse?

    suspend fun generatePrompt(image: ByteArray): String

    companion object {
        fun create(): SDAIPhotoService {
            return SDAIPhotoServiceImpl(
                client = HttpClient(Android) {
                    install(Logging) {
                        logger = Logger.DEFAULT
                        level = LogLevel.ALL
                    }
                    install(ContentNegotiation) {
                        json()
                    }
                }
            )
        }
    }
}
