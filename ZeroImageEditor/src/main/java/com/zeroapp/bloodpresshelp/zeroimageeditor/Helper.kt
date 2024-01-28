package com.zeroapp.bloodpresshelp.zeroimageeditor

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.Uri
import android.os.Build
import android.os.CountDownTimer
import android.provider.MediaStore
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max
import kotlinx.coroutines.delay

const val TAG = "asd123"

object Helper {

    private const val DATE_TIME_FORMAT_DD_MY_YYYY_HH_MM = "yyyyMMdd-HHmm"
    private const val DATE_TIME_FORMAT_DD_MY_YYYY = "yyyyMMdd"
    private const val DATE_TIME_FORMAT_MM_DD = "MMdd"

    fun convertImageUriToByte(context: Context, uri: Uri?): ByteArray? {
        var data: ByteArray? = null
        try {
            val cr: ContentResolver = context.contentResolver
            val inputStream: InputStream? = cr.openInputStream(uri!!)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
            data = byteArrayOutputStream.toByteArray()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        return data
    }

    fun reduceImageBitmap(imageBitmap: Bitmap, max: Int): Bitmap {
        val width = imageBitmap.width
        val height = imageBitmap.height
        val maxInput = max(width, height)
        if (maxInput < max) return imageBitmap
        val ratio = maxInput.toFloat().div(max)
        val newWidth = if (width == maxInput) {
            max
        } else width.div(ratio).toInt()
        val newHeight = if (height == maxInput) {
            max
        } else height.div(ratio).toInt()
        Log.d("asd123", "reduceImageBitmap, newSize: w = $newWidth - h = $newHeight")
        return Bitmap.createScaledBitmap(imageBitmap, newWidth, newHeight, false)
    }

    fun convertImageUriToBitmap(context: Context, uri: Uri?): Bitmap? {
        var bitmap: Bitmap? = null
        if (uri == null) return null
        try {
            val cr: ContentResolver = context.contentResolver
            val inputStream: InputStream? = cr.openInputStream(uri)
            bitmap = BitmapFactory.decodeStream(inputStream)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        Log.d("asd123", "convertImageUriToBitmap: bitmap = $bitmap")
        return bitmap
    }

    fun getImageUriFromStorage(context: Context): List<Uri> {
        val uriList = mutableListOf<Uri>()
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_MODIFIED
        )
        val selection = MediaStore.Images.Media.DISPLAY_NAME + " LIKE ?"
        val selectionArgs = arrayOf("${Constance.PREFIX_IMAGE_NAME}%")
        val sortOrder = "${MediaStore.Video.Media.DATE_MODIFIED} DESC"
        try {
            val cursor = context.contentResolver.query(
                collection, projection, selection, selectionArgs, sortOrder
            ) ?: return uriList
            Log.d("asd123", "query cursor")
            val idColumn = cursor.getColumnIndex(MediaStore.Images.Media._ID)
            val idName = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)
            val idDate = cursor.getColumnIndex(MediaStore.Images.Media.DATE_MODIFIED)
            Log.d("asd123", "column = $idColumn - $idName - $idDate")
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(idName)
                val date = cursor.getString(idDate)
                Log.d("asd123", "$id - $name - $date")

                val uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                uriList.add(uri)
            }
            cursor.close()
        } catch (e : Exception) {
            Log.e("asd123", "get gallery uri failed, exception = ${e.printStackTrace()}")
        }
        return uriList
    }

    fun Uri.convertToBitmap(context: Context): Bitmap {
        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, this))
        } else {
            MediaStore.Images.Media.getBitmap(context.contentResolver, this)
        }
        return bitmap.also {
            Log.d("asd123", "convertToBitmap = $this")
        }
    }

    suspend fun tryToGetBitmapFromUrl(urlImage: String, delayTime: Long): Bitmap? {
        var bitmap: Bitmap? = null
        var inputStream: InputStream
        for (i in 1..RemoteConfig.MAX_RETRY_TIME_GET_BITMAP) {
            Log.d("asd123", "tryToGetBitmapFromUrl, time #$i")
            try {
                inputStream = URL(urlImage).openStream()
                bitmap = BitmapFactory.decodeStream(inputStream)
//                val url = URL(urlImage)
//                bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream())
                if (bitmap != null) {
                    Log.d("asd123", "getBitmapFromUrlImage success!")
                    return bitmap
                }
            } catch (e: IOException) {
                Log.e(
                    "asd123",
                    "getBitmapFromUrl2 got exception: ${e.printStackTrace()} - ${e.message}"
                )
                e.printStackTrace()
            }
            delay(delayTime)
        }
        return null
    }

    fun Bitmap.convertToByteArray(): ByteArray {
        val output = ByteArrayOutputStream()
        this.compress(Bitmap.CompressFormat.JPEG, 100, output)
        return output.toByteArray()
    }

    fun Bitmap.resizeBitMap(): Bitmap {
        val width = this.width
        val height = this.height
        var newWidth = 0
        var newHeight = 0
        if (width > height) {
            newWidth = 512
            newHeight = height*512/width
        } else {
            newHeight = 512
            newWidth = width*512/height
        }
        Log.d("asd123", "resizeBitMap, w = $width - h = $height -> w = $newWidth - h = $newHeight")
        val output = Bitmap.createScaledBitmap(this, newWidth, newHeight, false)
        return output
    }

    fun Uri.convertToByteArrayWithDownSizeImage(context: Context): ByteArray {
        var data = byteArrayOf()
        try {
            val cr: ContentResolver = context.contentResolver
            val inputStream: InputStream? = cr.openInputStream(this)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val newBitmap = bitmap.resizeBitMap()
            val byteArrayOutputStream = ByteArrayOutputStream()
            newBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
            data = byteArrayOutputStream.toByteArray()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        return data
    }

    fun Uri.convertToBitmapWithDownSizeImage(context: Context): Bitmap? {
        var result: Bitmap? = null
        try {
            val cr: ContentResolver = context.contentResolver
            val inputStream: InputStream? = cr.openInputStream(this)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            result = bitmap.resizeBitMap()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        return result
    }

    fun checkInternetConnection(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val networkInfo = connectivityManager?.activeNetworkInfo
        return (networkInfo != null && networkInfo.isConnected).also {
            Log.d("asd123", "checkInternetConnection: $it")
        }
    }

    fun checkInternetConnectionWithMessage(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val networkInfo = connectivityManager?.activeNetworkInfo
        return (networkInfo != null && networkInfo.isConnected).also { isHaveConnect ->
            Log.d("asd123", "checkInternetConnection = $isHaveConnect")
            if (!isHaveConnect) {

//                DialogHelper.showCustomMessageDialog(
//                    context,
//                    R.string.no_internet_title,
//                    R.string.no_internet_content,
//                    R.string.try_again_button
//                )
            }
        }
    }

    fun checkConnectInternetCallback(context: Context) {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            // network is available for use
            override fun onAvailable(network: Network) {
                Log.d("asd123", "onAvailable")
                super.onAvailable(network)
            }

            // Network capabilities have changed for the network
            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                Log.d("asd123", "onCapabilitiesChanged")
                super.onCapabilitiesChanged(network, networkCapabilities)
                val unmetered = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
            }

            // lost network connection
            override fun onLost(network: Network) {
                Log.d("asd123", "onLost")
                super.onLost(network)
            }
        }
        val connectivityManager = context.getSystemService(ConnectivityManager::class.java) as ConnectivityManager
        connectivityManager.requestNetwork(networkRequest, networkCallback)
    }

    fun createCountdownCallback(timeout: Long, finish: () -> Unit) {
        object : CountDownTimer(timeout, 1) {
            override fun onTick(p0: Long) {

            }
            override fun onFinish() {
                finish.invoke()
            }
        }.start()
    }

    fun Int.getExactlyStep(): Int {
        return when {
            this < 25 -> 21
            this in 25..35 -> 31
            this in 35..45 -> 41
            else -> 51
        }.also {
            Log.d("asd123", "getExactlyStep: $this -> $it")
        }
    }

    fun convertMillisToDateTime(millis: Long): String {
        val format = SimpleDateFormat(DATE_TIME_FORMAT_DD_MY_YYYY_HH_MM, Locale.getDefault())
        val date = Date(millis)
        return format.format(date)
    }

    fun getDateToday(): String {
        val format = SimpleDateFormat(DATE_TIME_FORMAT_DD_MY_YYYY, Locale.getDefault())
        val date = Date(System.currentTimeMillis())
        return format.format(date)
    }
}