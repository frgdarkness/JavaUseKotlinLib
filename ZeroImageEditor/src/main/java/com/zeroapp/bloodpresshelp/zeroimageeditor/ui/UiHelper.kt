package com.zeroapp.bloodpresshelp.zeroimageeditor.ui

import android.app.Dialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.zeroapp.bloodpresshelp.zeroimageeditor.Helper
import com.zeroapp.bloodpresshelp.zeroimageeditor.R
import com.zeroapp.bloodpresshelp.zeroimageeditor.databinding.DialogLoadingBinding


object UiHelper {

    const val NOTIFY_CHANNEL_ID = "zero_notify_channel_id"
    const val CHANNEL_NAME = "ZeroAI"
    const val NOTIFY_ID = 1234
    var wasChannelBuilt = false

    fun View.show() {
        this.visibility = View.VISIBLE
    }

    fun View.hide() {
        this.visibility = View.INVISIBLE
    }

    fun View.gone() {
        this.visibility = View.GONE
    }

    fun dpToPx(context: Context, dp: Int): Int {
        val scale: Float = context.resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt().also {
            Log.d("asd123", "dpToPx: $dp -> $it")
        }
    }

//    fun createNotification(context: Context, title: String, message: String, imageBitmap: Bitmap?) {
//        // TODO
////        val intent = Intent(context, SplashActivity::class.java).apply {
////            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
////        }
////        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
//        if (!wasChannelBuilt) createNotificationChannel(context)
//
//        val largeIcon = imageBitmap ?: BitmapFactory. decodeResource (context.resources , R.drawable.app_icon_zeroai )
//        val builder = NotificationCompat.Builder(context, NOTIFY_CHANNEL_ID)
//            .setSmallIcon(R.drawable.ai_icon)
//            .setLargeIcon(largeIcon)
//            .setContentTitle(title)
//            .setContentText(message)
//            .setPriority(NotificationCompat.PRIORITY_HIGH)
////            .setContentIntent(pendingIntent)
//            .setAutoCancel(true)
//        with(NotificationManagerCompat.from(context)) {
//            // notificationId is a unique int for each notification that you must define
//            if (context.checkUserPermission(Manifest.permission.POST_NOTIFICATIONS)) {
//                notify(NOTIFY_ID, builder.build())
//            }
//        }
//    }

    fun showShortToast(content: Context, message: String) {
        Toast.makeText(content, message, Toast.LENGTH_SHORT).show()
    }

    private fun Context.checkUserPermission(permission: String): Boolean {
        val isGranted = ActivityCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
        Log.d("asd123", "checkUserPermission($permission) - $isGranted")
        return isGranted
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = CHANNEL_NAME
            val descriptionText = "Notification channel description"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(NOTIFY_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
        wasChannelBuilt = true
    }

    fun createLoadingDialog(context: Context, message: String? = null): Dialog {
        val dialog = Dialog(context)
        val binding = DialogLoadingBinding.inflate(LayoutInflater.from(context))
        dialog.apply {
            setContentView(binding.root)
            setCanceledOnTouchOutside(false)
        }
        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setGravity(Gravity.CENTER)
        }
        dialog.setWidthHeight(85, 25)
        message?.let {
            binding.tvTitle.text = message
        }
        binding.lottieView.apply {
            setAnimation(R.raw.loading)
            playAnimation()
        }
        return dialog
    }

    fun Dialog.setWidthHeight(widthPercentage: Int, heightPercentage: Int) {
        val dm = Resources.getSystem().displayMetrics
        val rect = dm.run { Rect(0, 0, widthPixels, heightPixels) }
        val percentWidth = rect.width() * (widthPercentage.toFloat() / 100)
        val percentHeight = rect.height() * (heightPercentage.toFloat() / 100)
//        Log.d("asd123", "percentWidth: $percentWidth - percentHeight: $percentHeight")
        this.window?.setLayout(percentWidth.toInt(), percentHeight.toInt())
    }

    fun loadImageToView(context: Context, imageUrl: String, imageView: ImageView,retryTime: Int = 3) {
        Log.d("asd123", "loadImage: $imageUrl")
        if (imageUrl.isEmpty()) return
        Glide.with(context)
            .asBitmap()
            .load(imageUrl)
            .placeholder(R.color.place_holder_color)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    imageView.setImageBitmap(resource)
                    Log.d(
                        "asd123",
                        "loadImage - onResourceReady, imageSize = ${resource.width} - ${resource.height}"
                    )
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    Log.d("asd123", "onLoadCleared")
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    Log.d("asd123", "onLoadFailed")
                    super.onLoadFailed(errorDrawable)
                    if (retryTime > 0) {
                        Helper.createCountdownCallback(2000) {
                            loadImageToView(context, imageUrl, imageView, retryTime - 1)
                        }
                    } else {

                    }
                }

                override fun onLoadStarted(placeholder: Drawable?) {
                    Log.d("asd123", "onLoadStarted")
                    super.onLoadStarted(placeholder)
                }

                override fun onStop() {
                    Log.d("asd123", "onStop")
                    super.onStop()
                }
            })
    }
}