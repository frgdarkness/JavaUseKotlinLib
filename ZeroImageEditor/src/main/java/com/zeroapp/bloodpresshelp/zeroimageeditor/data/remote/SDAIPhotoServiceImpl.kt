package com.zeroapp.bloodpresshelp.zeroimageeditor.data.remote

import android.util.Log
import com.zeroapp.bloodpresshelp.zeroimageeditor.RemoteConfig
import com.zeroapp.bloodpresshelp.zeroimageeditor.data.remote.model.GenerateImageToSDImageDataInput
import com.zeroapp.bloodpresshelp.zeroimageeditor.data.remote.model.GenerateSDImageDataInput
import com.zeroapp.bloodpresshelp.zeroimageeditor.data.remote.model.ImageResponseStatus
import com.zeroapp.bloodpresshelp.zeroimageeditor.data.remote.model.ImageType
import com.zeroapp.bloodpresshelp.zeroimageeditor.data.remote.model.SDFetchResponse
import com.zeroapp.bloodpresshelp.zeroimageeditor.data.remote.model.SDImageResponse
import com.zeroapp.bloodpresshelp.zeroimageeditor.data.remote.model.SDImageResponseTemp
import com.zeroapp.bloodpresshelp.zeroimageeditor.data.remote.model.SDRemoveBackgroundResponse
import com.zeroapp.bloodpresshelp.zeroimageeditor.data.remote.model.SDUpscaleResponse
import com.zeroapp.bloodpresshelp.zeroimageeditor.data.remote.model.convertToSDImageResponse
import com.zeroapp.bloodpresshelp.zeroimageeditor.data.remote.model.getImageResponseStatus
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.delay
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor


class SDAIPhotoServiceImpl(
    private val client: HttpClient
) : SDAIPhotoService {

    private var fetchImageRetryTime = 0

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun textToImage(input: GenerateSDImageDataInput?): SDImageResponse? {
        Log.d("asd123", "SDAIPhotoServiceImpl - textToImage - input = $input")
        if (input == null) return null
        return try {
            val formData = formData {
                append("key", "jglu5iuS2kiPc2KSv0Px1wo8JHMc328kT47Sfus7PJrRyz9QdzRexUoyjXu0")
                append("prompt", input.prompt)
                append("negative_prompt", input.negativePrompt)
                append("width", input.width)
                append("height", input.height)
                append("samples", "1")
                append("num_inference_steps", input.numInferenceSteps)
                append("safety_checker", "no")
                append("enhance_prompt", "yes")
                // Support seed later
                append("seed", "")
                append("guidance_scale", input.guidanceScale)
                append("multi_lingual", "no")
                append("panorama", "no")
                append("self_attention", "no")
                append("upscale", "no")
                append("embeddings_model", "")
                append("webhook", "")
                append("track_id", "")
                append(HttpHeaders.ContentType, "application/json")
            }
            Log.d("asd123", "request textToImage, formData = $formData")
            val response = client.submitFormWithBinaryData(
                "https://stablediffusionapi.com/api/v3/text2img",
                formData
            )
            Log.d("asd123", "response: $response")
            val responseData: String = response.body()
            Log.d("asd123", "responseData: $responseData")
            var sdResponse: SDImageResponse? = null
            try {
                val json = Json {
                    ignoreUnknownKeys = true
                    coerceInputValues = true
                    explicitNulls = false
                }
                sdResponse = json.decodeFromString(responseData)
                Log.d("asd123", "sdResponse = $sdResponse")
            } catch (e: SerializationException) {
                Log.e("asd123", "got error when text2image, error = ${e.printStackTrace()}")
            } catch (e: IllegalArgumentException) {
                Log.e("asd123", "got error when text2image, error = ${e.printStackTrace()}")
            } catch (e: Exception) {
                Log.e("asd123", "got other error when text2image, error = ${e.printStackTrace()}")
            }

            if (sdResponse == null) return null
            if (sdResponse.status.getImageResponseStatus() == ImageResponseStatus.SUCCESS) return sdResponse
            val fetchResult =
                fetchGenerateImageRequest(sdResponse.id, RemoteConfig.MAX_RETRY_TIME_FETCH_IMAGE)
            if (!fetchResult.isNullOrEmpty()) {
                return sdResponse.copy(
                    output = listOf(fetchResult)
                )
            }
//            for (i in 1..RemoteConfig.MAX_RETRY_TIME_FETCH_IMAGE) {
//                Log.d("asd123", "fetchImage, time($i)")
//                val fetchResult = fetchGenerateImageRequest(sdResponse.id, RemoteConfig.MAX_RETRY_TIME_FETCH_IMAGE)
//                if (!fetchResult.isNullOrEmpty()) {
//                    return sdResponse.copy(
//                        output = listOf(fetchResult)
//                    )
//                }
//                delay(RemoteConfig.DELAY_TIME_FETCH_IMAGE)
//            }
            null
        } catch (e: Exception) {
            Log.e("asd123", "got error when text2image, error = ${e.printStackTrace()}")
            null
        }
    }

    override suspend fun textToImageWithModel(input: GenerateSDImageDataInput?): SDImageResponse? {
        Log.d("asd123", "call textToImageWithModel, input = $input")
        if (input == null) return null
        val formData = formData {
            append("key", "jglu5iuS2kiPc2KSv0Px1wo8JHMc328kT47Sfus7PJrRyz9QdzRexUoyjXu0")
            append("model_id", input.modelId)
            append("prompt", input.prompt)
            append("negative_prompt", input.negativePrompt)
            append("width", input.width)
            append("height", input.height)
            append("samples", "1")
            append("num_inference_steps", input.numInferenceSteps)
            append("safety_checker", "no")
            append("enhance_prompt", "yes")
            append("seed", "")
            append("guidance_scale", input.guidanceScale)
            append("scheduler", input.scheduler)
            append("algorithm_type", input.algorithmType)
            append("multi_lingual", "no")
            append("panorama", "no")
            append("self_attention", "no")
            append("upscale", "no")
            append("embeddings_model", "")
            append("webhook", "")
            append("track_id", "")
            if (input.loraModel.isNotEmpty()) {
                append("lora_model", input.loraModel)
                append("lora_strength", 0.5f)
            }
            append("tomesd", "yes")
            append("use_karras_sigmas", "yes")
            append(HttpHeaders.ContentType, "application/json")
        }
        val response = client.submitFormWithBinaryData(
            "https://stablediffusionapi.com/api/v4/dreambooth",
            formData
        )
        Log.d("asd123", "dreambooth response: $response")
        val responseData: String = response.body()
        Log.d("asd123", "dreambooth responseData: $responseData")
        var sdResponse: SDImageResponse? = null

        try {
            val json = Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
                explicitNulls = false
            }
            sdResponse = json.decodeFromString(responseData)
            Log.d("asd123", "sdResponse = $sdResponse")
        } catch (e: SerializationException) {
            Log.e(
                "asd123",
                "got error when call dreambooth, SerializationException error = ${e.printStackTrace()}"
            )
        } catch (e: IllegalArgumentException) {
            Log.e(
                "asd123",
                "got error when call dreambooth, IllegalArgumentException error = ${e.printStackTrace()}"
            )
        } catch (e: Exception) {
            Log.e("asd123", "got other error when call dreambooth, error = ${e.printStackTrace()}")
        }
        if (sdResponse == null) return null

        if (sdResponse.status.getImageResponseStatus() == ImageResponseStatus.SUCCESS) return sdResponse
//        fetchImageRetryTime = 0
//        var fetchResult = fetchGenerateImageRequest(sdResponse.id, 15)
//        if (!fetchResult.isNullOrEmpty()) {
//            return sdResponse.copy(
//                output = listOf(fetchResult)
//            )
//        }


        for (i in 1..RemoteConfig.MAX_RETRY_TIME_FETCH_IMAGE) {
            Log.d("asd123", "fetchImage, time($i)")
            val fetchResult = fetchGenerateImageRequest(sdResponse.id)
            if (!fetchResult.isNullOrEmpty()) {
                return sdResponse.copy(
                    output = listOf(fetchResult)
                )
            }
            delay(RemoteConfig.DELAY_TIME_FETCH_IMAGE)
        }
        return null
    }

    override suspend fun imageToImage(input: GenerateImageToSDImageDataInput?): SDImageResponse? {
        Log.d("asd123", "call imageToImage")
        if (input == null) return null
        val formData = formData {
            append("key", "jglu5iuS2kiPc2KSv0Px1wo8JHMc328kT47Sfus7PJrRyz9QdzRexUoyjXu0")
            append("controlnet_model", input.controlNetModel)
            append("controlnet_type", input.controlNetType)
            append("auto_hint", "yes")
            append("guess_mode", "yes")
            append("model_id", input.modelId)
            append("prompt", input.prompt)
            append("negative_prompt", input.negativePrompt)
            append("init_image", input.initImage)
            append("mask_image", "")
            append("width", "512")
            append("height", "512")
            append("samples", "1")
            append("scheduler", input.scheduler)
            append("num_inference_steps", input.numInferenceSteps)
            append("safety_checker", "no")
            append("enhance_prompt", "yes")
            append("guidance_scale", input.guidanceScale)
            append("controlnet_conditioning_scale", input.controlNetScale)
            append("strength", input.strength)
            if (input.loraModel.isNotEmpty() && input.loraStrength != 0f) {
                append("lora_model", input.loraModel)
                append("lora_strength", input.loraStrength)
            }
            append("clip_skip", "2")
            append("tomesd", "yes")
            append("use_karras_sigmas", "yes")
            append("vae", "")
            append("embeddings_model", "")
            append("seed", "")
            append("webhook", "")
            append("track_id", "")
//                append("multi_lingual", "no")
//                append("panorama", "no")
//                append("self_attention", "no")
//                append("upscale", "no")
            append(HttpHeaders.ContentType, "application/json")
        }
        val response = client.submitFormWithBinaryData(
            "https://stablediffusionapi.com/api/v5/controlnet",
            formData
        )
        Log.d("asd123", "img2imgResponse: $response")
        val responseData: String = response.body()
        Log.d("asd123", "img2imgResponseData: $responseData")
        var sdResponse: SDImageResponse? = null
        var sdResponseTemp: SDImageResponseTemp? = null
        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            explicitNulls = false
        }
        try {
            Log.d("asd123", "parseJson")
            sdResponse = json.decodeFromString(responseData)
            Log.d("asd123", "img2imgSDResponse = $sdResponse")
        } catch (e: SerializationException) {
            Log.e("asd123", "got error when image2image, error = ${e.printStackTrace()}")
        } catch (e: IllegalArgumentException) {
            Log.e("asd123", "got error when image2image, error = ${e.printStackTrace()}")
        } catch (e: Exception) {
            Log.e("asd123", "got other error when image2image, error = ${e.printStackTrace()}")
        }
        if (sdResponse == null) {
            try {
                Log.d("asd123", "try parse to sdResponseTemp")
                sdResponseTemp = json.decodeFromString(responseData)
                Log.d("asd123", "img2imgSDResponseTemp = $sdResponseTemp")
                if (sdResponseTemp != null) sdResponse =
                    sdResponseTemp.convertToSDImageResponse()
            } catch (e: SerializationException) {
                Log.e("asd123", "got error when image2image, error = ${e.printStackTrace()}")
            } catch (e: IllegalArgumentException) {
                Log.e("asd123", "got error when image2image, error = ${e.printStackTrace()}")
            } catch (e: Exception) {
                Log.e(
                    "asd123",
                    "got other error when image2image, error = ${e.printStackTrace()}"
                )
            }
        }
        Log.d("asd123", "parseJsonDone")
        if (sdResponse == null) return null
        if (sdResponse.status.getImageResponseStatus() == ImageResponseStatus.SUCCESS) return sdResponse
        for (i in 1..10) {
            Log.d("asd123", "fetchImage (${sdResponse.id}) - time($i)")
            val fetchResult = fetchGenerateImageRequest(sdResponse.id)
            Log.d("asd123", "fetchResult img2img = $fetchResult")
            if (!fetchResult.isNullOrEmpty()) {
                Log.d("asd123", "stop fetch")
                return sdResponse.copy(
                    output = listOf(fetchResult)
                )
            }
            Log.d("asd123", "wait delay")
            delay(RemoteConfig.DELAY_TIME_FETCH_IMAGE)
            Log.d("asd123", "delay done")
        }
        Log.d("asd123", "finishGenerate")
        return null
    }

    override suspend fun upscaleImage(
        imageUrl: String,
        scale: Int,
        imageType: ImageType
    ): SDUpscaleResponse? {
        return try {
            val formData = formData {
                append("key", "jglu5iuS2kiPc2KSv0Px1wo8JHMc328kT47Sfus7PJrRyz9QdzRexUoyjXu0")
                append("url", imageUrl)
                append("scale", scale)
                append("webhook", "")
//                append("face_enhance", "false")
                if (imageType == ImageType.ANIME) {
                    append("model_id", "RealESRGAN_x4plus_anime_6B")
                }
                append(HttpHeaders.ContentType, "application/json")
            }
            Log.d("asd123", "upscaleImage, formData = $formData")
            val response = client.submitFormWithBinaryData(
                "https://stablediffusionapi.com/api/v3/super_resolution",
                formData
            )
            val responseData = response.body<String>()
            var sdUpscaleResponse: SDUpscaleResponse? = null
            Log.d("asd123", "upscaleImageResponseData = $responseData")
            try {
                val json = Json {
                    ignoreUnknownKeys = true
                    coerceInputValues = true
                    explicitNulls = false
                }
                sdUpscaleResponse =
                    json.decodeFromString<SDUpscaleResponse>(responseData)
            } catch (e: SerializationException) {
                Log.e(
                    "asd123",
                    "got error when upscaleImage, SerializationException error = ${e.printStackTrace()}"
                )
            } catch (e: IllegalArgumentException) {
                Log.e(
                    "asd123",
                    "got error when upscaleImage, IllegalArgumentException error = ${e.printStackTrace()}"
                )
            } catch (e: Exception) {
                Log.e("asd123", "got other error when upscaleImage, error = ${e.printStackTrace()}")
            }
            if (sdUpscaleResponse == null) return null
            if (sdUpscaleResponse.status == ImageResponseStatus.SUCCESS.value) return sdUpscaleResponse
            for (i in 1..RemoteConfig.MAX_RETRY_TIME_FETCH_IMAGE) {
                Log.d("asd123", "fetchImage, time($i)")
                val fetchResult = fetchGenerateImageRequestOkhttp(sdUpscaleResponse.id)
                if (!fetchResult.isNullOrEmpty()) {
                    return sdUpscaleResponse.copy(
//                        output = listOf(fetchResult)
                        output = fetchResult
                    )
                }
                delay(RemoteConfig.DELAY_TIME_FETCH_IMAGE)
            }
            Log.d("asd123", "sdUpscaleResponse = $sdUpscaleResponse")
            sdUpscaleResponse
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun upscaleImageOkhttp(
        imageUrl: String,
        scale: Int,
        imageType: ImageType
    ): SDUpscaleResponse? {
        val logger = HttpLoggingInterceptor()
        logger.level = (HttpLoggingInterceptor.Level.BASIC)
        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(logger)
            .build()
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("key", "jglu5iuS2kiPc2KSv0Px1wo8JHMc328kT47Sfus7PJrRyz9QdzRexUoyjXu0")
            .addFormDataPart("url", imageUrl)
            .addFormDataPart("scale", "$scale")
            .addFormDataPart("webhook", "")
            .build()

        val request = Request.Builder()
            .url("https://stablediffusionapi.com/api/v3/super_resolution")
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            Log.d("asd123", "got error when upscale")
            return null
        }
        val responseData = response.body?.string()
        Log.d("asd123", "responseData = $responseData")
        if (responseData == null) return null
        var sdUpscaleResponse: SDUpscaleResponse? = null
        try {
            val json = Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
                explicitNulls = false
            }
            sdUpscaleResponse =
                json.decodeFromString<SDUpscaleResponse>(responseData)
        } catch (e: Exception) {
            Log.e("asd123", "got other error when upscaleImage, error = ${e.message}")
        }
        if (sdUpscaleResponse == null) return null
        if (sdUpscaleResponse?.status == ImageResponseStatus.SUCCESS.value) return sdUpscaleResponse
        if (responseData.contains("processing")) {
            for (i in 1..RemoteConfig.MAX_RETRY_TIME_FETCH_IMAGE) {
                Log.d("asd123", "fetchImage, time($i)")
                val fetchResult = fetchGenerateImageRequestNew(sdUpscaleResponse.id)
                if (!fetchResult.isNullOrEmpty()) {
                    return sdUpscaleResponse.copy(output = fetchResult)
                }
                delay(RemoteConfig.DELAY_TIME_FETCH_IMAGE)
            }
        }
        return null
    }

    override suspend fun removeBackgroundOkhttp(imageUrl: String): SDRemoveBackgroundResponse? {
        val logger = HttpLoggingInterceptor()
        logger.level = (HttpLoggingInterceptor.Level.BASIC)
        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(logger)
            .build()
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("key", "jglu5iuS2kiPc2KSv0Px1wo8JHMc328kT47Sfus7PJrRyz9QdzRexUoyjXu0")
            .addFormDataPart("image", imageUrl)
            .addFormDataPart("webhook", "")
//            .addFormDataPart("post_process_mask", "")
//            .addFormDataPart("only_mask", "")
//            .addFormDataPart("alpha_matting", "")
            .build()

        val request = Request.Builder()
            .url("https://stablediffusionapi.com/api/v5/removebg_mask")
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            Log.d("asd123", "got error when upscale")
            return null
        }
        val responseData = response.body?.string()
        Log.d("asd123", "responseData = $responseData")
        if (responseData == null) return null
        var sdUpscaleResponse: SDRemoveBackgroundResponse? = null
        try {
            val json = Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
                explicitNulls = false
            }
            sdUpscaleResponse =
                json.decodeFromString<SDRemoveBackgroundResponse>(responseData)
        } catch (e: Exception) {
            Log.e("asd123", "got other error when upscaleImage, error = ${e.message}")
        }
        if (sdUpscaleResponse == null) return null
        if (sdUpscaleResponse?.status == ImageResponseStatus.SUCCESS.value) return sdUpscaleResponse
        if (responseData.contains("processing")) {
            for (i in 1..RemoteConfig.MAX_RETRY_TIME_FETCH_IMAGE) {
                Log.d("asd123", "fetchImage, time($i)")
                val fetchResult = fetchGenerateImageRequestNew(sdUpscaleResponse.id)
                if (!fetchResult.isNullOrEmpty()) {
                    return sdUpscaleResponse.copy(output = listOf(fetchResult))
                }
                delay(RemoteConfig.DELAY_TIME_FETCH_IMAGE)
            }
        }
        return null
    }

    fun newUpscaleImage(imageUrl: String, scale: Int, imageType: ImageType): SDUpscaleResponse? {
        val logger = HttpLoggingInterceptor()
        logger.level = (HttpLoggingInterceptor.Level.BASIC)
        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(logger)
            .build()
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("key", "jglu5iuS2kiPc2KSv0Px1wo8JHMc328kT47Sfus7PJrRyz9QdzRexUoyjXu0")
            .addFormDataPart("url", imageUrl)
            .addFormDataPart("webhook", "")
//            .addFormDataPart("post_process_mask", "")
//            .addFormDataPart("only_mask", "")
//            .addFormDataPart("alpha_matting", "")
            .build()
        try {
            val request = Request.Builder()
                .url("https://stablediffusionapi.com/api/v5/removebg_mask")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyString = response.body?.string()
                    Log.d("asd123", "bodyString=$bodyString")
                } else {
                    Log.e("asd123", response.message)
                    // Handle Api Errors here
                }
            }
        } catch (e: Exception) {
            Log.e("asd123", e.message.toString())
            // Handle Exceptions here
        }
        return null
    }

    override suspend fun upscaleImageOld(imageUrl: String, scale: Int): SDUpscaleResponse? {
        return try {
            val formData = formData {
                append("key", "jglu5iuS2kiPc2KSv0Px1wo8JHMc328kT47Sfus7PJrRyz9QdzRexUoyjXu0")
                append("url", imageUrl)
                append("scale", scale)
                append("webhook", "")
                append("face_enhance", "false")
                append("model_id", "RealESRGAN_x4plus_anime_6B")
                append(HttpHeaders.ContentType, "application/json")
            }
            Log.d("asd123", "upscaleImage, formData = $formData")
            val response = client.submitFormWithBinaryData(
                "https://stablediffusionapi.com/api/v3/super_resolution",
                formData
            )
            Log.d("asd123", "upscaleImageResponse1 = $response")
            val responseData = response.body<String>()
            val sdUpscaleResponse: SDUpscaleResponse = response.body()
            Log.d("asd123", "upscaleImageResponse2 = $sdUpscaleResponse")
            sdUpscaleResponse
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun removeBackground(imageUrl: String): SDRemoveBackgroundResponse? {
        Log.d("asd123", "removeBackground: $imageUrl")
        val formData = formData {
            append("key", "jglu5iuS2kiPc2KSv0Px1wo8JHMc328kT47Sfus7PJrRyz9QdzRexUoyjXu0")
            append("image", imageUrl)
//                append("post_process_mask", "false")
//                append("only_mask", "false")
//                append("alpha_matting", "false")
            append("webhook", "")
            append("track_id", "")
            append(HttpHeaders.ContentType, "application/json")
        }
        Log.d("asd123", "removeBackground, formData = $formData")
        val response = client.submitFormWithBinaryData(
            "https://stablediffusionapi.com/api/v5/removebg_mask",
            formData
        )
        val responseData = response.body<String>()
        var sdRemoveBackgroundResponse: SDRemoveBackgroundResponse? = null
        Log.d("asd123", "RemoveBackgroundResponseString = $responseData")
        try {
            val json = Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
                explicitNulls = false
            }
            sdRemoveBackgroundResponse =
                json.decodeFromString<SDRemoveBackgroundResponse>(responseData)
        } catch (e: SerializationException) {
            Log.e(
                "asd123",
                "got error when removeBackground, SerializationException error = ${e.printStackTrace()}"
            )
        } catch (e: IllegalArgumentException) {
            Log.e(
                "asd123",
                "got error when removeBackground, IllegalArgumentException error = ${e.printStackTrace()}"
            )
        } catch (e: Exception) {
            Log.e("asd123", "got other error when call dreambooth, error = ${e.printStackTrace()}")
        }
        if (sdRemoveBackgroundResponse == null) return null
        if (sdRemoveBackgroundResponse.status == ImageResponseStatus.SUCCESS.value) return sdRemoveBackgroundResponse
        for (i in 1..RemoteConfig.MAX_RETRY_TIME_FETCH_IMAGE) {
            Log.d("asd123", "fetchImage, time($i)")
            val fetchResult = fetchGenerateImageRequest(sdRemoveBackgroundResponse.id)
            if (!fetchResult.isNullOrEmpty()) {
                return sdRemoveBackgroundResponse.copy(
                    output = listOf(fetchResult)
                )
            }
            delay(RemoteConfig.DELAY_TIME_FETCH_IMAGE)
        }
        Log.d("asd123", "RemoveBackgroundResponseString = $sdRemoveBackgroundResponse")
        return null
    }

    suspend fun fetchGenerateImageRequestOkhttp(
        requestId: Long
    ): String? {
        val logger = HttpLoggingInterceptor()
        logger.level = (HttpLoggingInterceptor.Level.BASIC)
        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(logger)
            .build()
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("key", "jglu5iuS2kiPc2KSv0Px1wo8JHMc328kT47Sfus7PJrRyz9QdzRexUoyjXu0")
            .addFormDataPart("request_id", "$requestId")
            .build()
        val request = Request.Builder()
            .url("https://stablediffusionapi.com/api/v4/dreambooth/fetch")
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()

        if (response.isSuccessful) {
            val responseData = response.body?.string()
            Log.d("asd123", "fetch responseData = $responseData")
            if (responseData?.contains("processing") == true) return null else {}
                try {
                    val json = Json {
                        ignoreUnknownKeys = true
                        coerceInputValues = true
                        explicitNulls = false
                    }
                    val fetchResponse: SDFetchResponse = json.decodeFromString(responseData ?: "")
                    Log.d("asd123", "fetchResponse = $fetchResponse")
                    return fetchResponse.output.firstOrNull()
                } catch (e: java.lang.Exception) {
                    Log.e("asd123", "got exception when decode fetch response")
                }

        } else {
            Log.e("asd123", response.message)
            return null
            // Handle Api Errors here
        }
        return null
    }


    suspend fun fetchGenerateImageRequest(
        requestId: Long,
        retryTime: Int = RemoteConfig.MAX_RETRY_TIME_FETCH_IMAGE
    ): String? {
        Log.d("asd123", "fetchGenerateImageRequest($requestId), retryTime = $retryTime")
        if (retryTime < 0) return null
        val formData = formData {
            append("key", "jglu5iuS2kiPc2KSv0Px1wo8JHMc328kT47Sfus7PJrRyz9QdzRexUoyjXu0")
            append("request_id", requestId)
            append(HttpHeaders.ContentType, "application/json")
        }
        val response = client.submitFormWithBinaryData(
            "https://stablediffusionapi.com/api/v4/dreambooth/fetch",
            formData
        )
        val responseData: String = response.body()
        Log.d("asd123", "fetchGenerateImageRequest -> response = $responseData")
        if (responseData.contains("processing")) {
            Log.d("asd123", "fetchGenerateImageRequest -> processing")
            delay(4000)
            return fetchGenerateImageRequest(requestId, retryTime - 1)
        }
        Log.d("asd123", "handleDecodeResponse")
        try {
            val json = Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
                explicitNulls = false
            }
            val fetchResponse: SDFetchResponse = json.decodeFromString(responseData)
            Log.d("asd123", "fetchResponse = $fetchResponse")
            return fetchResponse.output.firstOrNull()
        } catch (e: SerializationException) {
            Log.e(
                "asd123",
                "got SerializationException when text2image, error = ${e.printStackTrace()}"
            )
        } catch (e: IllegalArgumentException) {
            Log.e(
                "asd123",
                "got IllegalArgumentException when text2image, error = ${e.printStackTrace()}"
            )
        } catch (e: Exception) {
            Log.e("asd123", "got other error when text2image, error = ${e.printStackTrace()}")
        }
        Log.d("asd123", "fetchGenerateImageRequest -> return null")
        return null
    }

    suspend fun fetchGenerateImageRequestNew(requestId: Long): String? {
        Log.d("asd123", "fetchGenerateImageRequest($requestId)")

        val formData = formData {
            append("key", "jglu5iuS2kiPc2KSv0Px1wo8JHMc328kT47Sfus7PJrRyz9QdzRexUoyjXu0")
            append("request_id", requestId)
            append(HttpHeaders.ContentType, "application/json")
        }
        var response: HttpResponse? = null
        var retryTime = 0
        while (retryTime < 15) {
            Log.d("asd123", "fetchGenerateImageRequest($requestId) #$retryTime")
            response = client.submitFormWithBinaryData(
                "https://stablediffusionapi.com/api/v4/dreambooth/fetch",
                formData
            )
            val responseData: String = response.body()
            Log.d("asd123", "fetchGenerateImageRequest -> response = $responseData")
            if (!responseData.contains("processing")) {
                break
            }
            retryTime++
            fetchImageRetryTime = retryTime
            delay(5000)
        }
        val responseData: String = response?.body() ?: return null
        Log.d("asd123", "handleDecodeResponse")
        try {
            val json = Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
                explicitNulls = false
            }
            val fetchResponse: SDFetchResponse = json.decodeFromString(responseData)
            Log.d("asd123", "fetchResponse = $fetchResponse")
            return fetchResponse.output.firstOrNull()
        } catch (e: SerializationException) {
            Log.e(
                "asd123",
                "got SerializationException when text2image, error = ${e.printStackTrace()}"
            )
        } catch (e: IllegalArgumentException) {
            Log.e(
                "asd123",
                "got IllegalArgumentException when text2image, error = ${e.printStackTrace()}"
            )
        } catch (e: Exception) {
            Log.e("asd123", "got other error when text2image, error = ${e.printStackTrace()}")
        }
        Log.d("asd123", "fetchGenerateImageRequest -> return null")
        return null
    }

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun fetchGenerateImageRequest(requestId: Long): String? {
        Log.d("asd123", "fetchGenerateImageRequest: $requestId")

        val formData = formData {
            append("key", "jglu5iuS2kiPc2KSv0Px1wo8JHMc328kT47Sfus7PJrRyz9QdzRexUoyjXu0")
            append("request_id", requestId)
            append(HttpHeaders.ContentType, "application/json")
        }
        val response = client.submitFormWithBinaryData(
            "https://stablediffusionapi.com/api/v4/dreambooth/fetch",
            formData
        )
        val responseData: String = response.body()
        Log.d("asd123", "fetchGenerateImageRequest -> response = $responseData")
        if (responseData.contains("processing")) return null
        try {
            val json = Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
                explicitNulls = false
            }
            val fetchResponse: SDFetchResponse = json.decodeFromString(responseData)
            Log.d("asd123", "fetchResponse = $fetchResponse")
            return fetchResponse.output.firstOrNull()
        } catch (e: SerializationException) {
            Log.e(
                "asd123",
                "got SerializationException when text2image, error = ${e.printStackTrace()}"
            )
        } catch (e: IllegalArgumentException) {
            Log.e(
                "asd123",
                "got IllegalArgumentException when text2image, error = ${e.printStackTrace()}"
            )
        } catch (e: Exception) {
            Log.e("asd123", "got other error when text2image, error = ${e.printStackTrace()}")
        }
        return null
    }

    override suspend fun generatePrompt(image: ByteArray): String {
        return try {
            val formData = formData {
                append("key", "2e747f4f-f487-4421-b7f9-8cae82ec7829")
                append("model_version", 1)
                append("image", image, Headers.build {
                    append(HttpHeaders.ContentType, "image/jpeg")
                    append(HttpHeaders.ContentDisposition, "filename=image.jpg")
                })
            }
            val response =
                client.submitFormWithBinaryData("https://art.dreamapp.cloud/interrogator", formData)
            val data: String = response.body()
            Log.d("asd123", "response: $response - data: $data")
            return data
        } catch (e: Exception) {
            Log.e("asd123", "generatePrompt got Exception: ${e.printStackTrace()}")
            "empty"
        }
    }

}