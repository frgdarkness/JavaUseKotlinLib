package com.zeroapp.bloodpresshelp.zeroimageeditor.ui.removebackground

import android.app.Dialog
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.firebase.storage.FirebaseStorage
import com.zeroapp.bloodpresshelp.zeroimageeditor.Constance
import com.zeroapp.bloodpresshelp.zeroimageeditor.Helper
import com.zeroapp.bloodpresshelp.zeroimageeditor.Helper.convertToByteArray
import com.zeroapp.bloodpresshelp.zeroimageeditor.R
import com.zeroapp.bloodpresshelp.zeroimageeditor.RemoteConfig
import com.zeroapp.bloodpresshelp.zeroimageeditor.databinding.ActivityRemoveBackgroundResultBinding
import com.zeroapp.bloodpresshelp.zeroimageeditor.ui.UiHelper
import kotlin.random.Random

class RemoveBackgroundResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRemoveBackgroundResultBinding
    private lateinit var viewModel: RemoveBackgroundResultViewModel
    private var loadingDialog: Dialog? = null
    private var isUserEarnedReward = false
    private var prompt = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRemoveBackgroundResultBinding.inflate(layoutInflater)
        window?.statusBarColor = ContextCompat.getColor(this, R.color.white)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this)[RemoveBackgroundResultViewModel::class.java]
        val imageUri = intent.getParcelableExtra<Uri>("IMAGE_URI")
        prompt = intent.getStringExtra("PROMPT") ?:  "Make background special"
        Glide.with(this).load(imageUri).into(binding.imageResult)
        initView(imageUri)
        initEvent()
        observerViewModel()
    }

    private fun initView(imageUri: Uri?) {
        showLoadingDialog()
        Glide.with(this)
            .asBitmap()
            .load(imageUri)
            .placeholder(R.color.place_holder_color)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    val newBitmap = Helper.reduceImageBitmap(resource, RemoteConfig.MAX_SIZE_SELECTED_IMAGE)
                    binding.imageResult.setImageBitmap(newBitmap)
                    uploadAndRemoveBackground(newBitmap)
//                    viewModel.removeBackgroundByClip(newBitmap, prompt)
//                    viewModel.clipApi(newBitmap, prompt)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    Log.d("asd123", "onLoadCleared")
                }
            })
        binding.apply {
            layoutActionBar.tvTitle.text = "Remove BG"
        }
    }

    private fun initEvent() {
        binding.apply {
            layoutActionBar.btnMenu.setOnClickListener {
                onBackPressed()
            }
            layoutActionBar.cardViewSave.setOnClickListener {

                saveImage(viewModel.getOutputImage()?.convertToByteArray())
            }
        }
    }

    private fun observerViewModel() {
        viewModel.removedBackgroundImageUrl.observe(this) {
            loadResultImage(it)
        }
        viewModel.isServerError.observe(this) { isError ->
            if (isError) {
                dismissLoadingDialog()
                UiHelper.showShortToast(this, "Got error from server!")
            }
        }
        viewModel.clipImageOutput.observe(this) { output ->
            dismissLoadingDialog()
            if (output != null) {
                Glide.with(this).load(output).into(binding.imageResult)
            } else {
                Log.d("asd123", "output null")
            }
        }
        viewModel.bitmapOutput.observe(this) {
            dismissLoadingDialog()
            Glide.with(this).load(it).into(binding.imageResult)

        }
    }

    private fun uploadAndRemoveBackground(imageBitmap: Bitmap) {
        val currentTime = System.currentTimeMillis()
        val dateTime = Helper.convertMillisToDateTime(currentTime)
        val name = dateTime + "-" + Random.nextInt(1000, 9999).toString()
        val storeRef = FirebaseStorage.getInstance().getReference("images/$name")

        storeRef.putBytes(imageBitmap.convertToByteArray())
            .addOnSuccessListener {
                Log.d("asd123", "upload success")
                storeRef.downloadUrl.addOnSuccessListener {
                    Log.d("asd123", "downloadUrl success, url = $it")
                    viewModel.removeBackground(it.toString())
                }.addOnFailureListener {
                    Log.d("asd123", "downloadUrl fail")
                }
            }
            .addOnFailureListener {
                Log.d("asd123", "upload failed, $it, ${it.message}, ${it.printStackTrace()}")
            }
    }

    private fun loadResultImage(imageUrl: String, retryTime: Int = 3) {
        Log.d("asd123", "loadResultImage: $imageUrl")
        Glide.with(this)
            .asBitmap()
            .load(imageUrl)
            .placeholder(R.color.place_holder_color)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    Log.d(
                        "asd123",
                        "loadImage - onResourceReady, imageSize = ${resource.width} - ${resource.height}"
                    )
                    dismissLoadingDialog()
                    binding.imageResult.setImageBitmap(resource)
                    viewModel.setOutputImage(resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    Log.d("asd123", "onLoadCleared")
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    Log.d("asd123", "onLoadImageResultFailed")
                    super.onLoadFailed(errorDrawable)
                    if (retryTime > 0) {
                        Helper.createCountdownCallback(2000) {
                            loadResultImage(imageUrl, retryTime - 1)
                        }
                    } else {
                        UiHelper.showShortToast(
                            this@RemoveBackgroundResultActivity,
                            "Got error from server! Please try again!"
                        )
                        dismissLoadingDialog()
                    }
                }

            })
    }

    private fun showLoadingDialog() {
        if (loadingDialog != null) {
            loadingDialog?.show()
            return
        }
        loadingDialog = UiHelper.createLoadingDialog(this, "Removing background...")
        loadingDialog?.show()
    }

    private fun dismissLoadingDialog() {
        loadingDialog?.dismiss()
    }

    private fun saveImage(data: ByteArray?) {
        if (viewModel.checkImageDownloaded()) {
            UiHelper.showShortToast(applicationContext, "Image is saved!")
            return
        }
        if (data == null) {
            UiHelper.showShortToast(applicationContext, "Save image failed!")
            return
        }
        Log.d("asd123", "saveImage")
        try {
            val contentResolver = contentResolver
            val image: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }
            val contentValues = ContentValues()
            contentValues.put(
                MediaStore.Images.Media.DISPLAY_NAME,
                Constance.PREFIX_IMAGE_NAME + System.currentTimeMillis().toString() + ".jpg"
            )
            contentValues.put(MediaStore.Images.Media.MIME_TYPE, "images/*")
            val uri = contentResolver.insert(image, contentValues) ?: return
            val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
            contentResolver.openOutputStream(uri)?.let {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                viewModel.setDownloadImageStatus(true)
                it.close()
                UiHelper.showShortToast(applicationContext, "Save image successful!")
            }
            dismissLoadingDialog()
            viewModel.setDownloadImageStatus(true)
        } catch (e: Exception) {
            Log.e("asd123", "Images not saved, exception: ${e.printStackTrace()}")
            UiHelper.showShortToast(applicationContext, "Download failed!")
        }
    }
}