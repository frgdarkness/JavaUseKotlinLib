package com.zeroapp.bloodpresshelp.zeroimageeditor.ui.enhancer

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.firebase.storage.FirebaseStorage
import com.zeroai.photoeditorai.ui.enhancer.EnhancerResultViewModel
import com.zeroapp.bloodpresshelp.zeroimageeditor.Constance
import com.zeroapp.bloodpresshelp.zeroimageeditor.Helper
import com.zeroapp.bloodpresshelp.zeroimageeditor.Helper.convertToByteArray
import com.zeroapp.bloodpresshelp.zeroimageeditor.R
import com.zeroapp.bloodpresshelp.zeroimageeditor.data.remote.model.ImageType
import com.zeroapp.bloodpresshelp.zeroimageeditor.databinding.ActivityEnhancerResultBinding
import com.zeroapp.bloodpresshelp.zeroimageeditor.ui.UiHelper
import kotlin.math.max
import kotlin.random.Random

class EnhancerResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEnhancerResultBinding
    private lateinit var viewModel: EnhancerResultViewModel
    private var loadingDialog: Dialog? = null
    private var isDisplayingImageBefore = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEnhancerResultBinding.inflate(layoutInflater)
        window?.statusBarColor = ContextCompat.getColor(this, R.color.white)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this)[EnhancerResultViewModel::class.java]
        observeViewModel()
//        val bundle = intent.getBundleExtra("ENHANCER_DATA")
        val imageUri = intent.getParcelableExtra<Uri>("IMAGE_URI")
        Log.d("asd123", "getUri = ${imageUri.toString()}")
        getDataFromIntent()
        initView()
        initEvent()
    }

    private fun getDataFromIntent() {
        val imageUri = intent.getParcelableExtra<Uri>(Constance.IMAGE_URI_DATA)
        val scaleValue = intent?.getIntExtra(Constance.SCALE_VALUE_DATA, 2) ?: 2
        val imageTypeString = intent?.getStringExtra(Constance.IMAGE_TYPE_DATA)
        val imageType = if (imageTypeString == ImageType.PHOTO.name) ImageType.PHOTO else ImageType.ANIME
        viewModel.setScale(scaleValue)
        viewModel.setImageType(imageType)
        Log.d("asd123", "scaleValue: $scaleValue - $imageType")
        if (imageUri != null) loadInputImage(imageUri)
    }

    private fun initView() {
        binding.apply {
            layoutActionBar.tvTitle.text = "Enhancer"
//            layoutActionBar.cardViewPro.gone()
        }

    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initEvent() {
        binding.layoutActionBar.cardViewSave.setOnClickListener {
            saveImage(viewModel.getOutputImage()?.convertToByteArray())
        }
        binding.layoutActionBar.btnMenu.setOnClickListener {
            onBackPressed()
        }
    }

    private fun loadInputImage(imageUri: Uri) {
        showLoadingDialog()
        Glide.with(this)
            .asBitmap()
            .load(imageUri)
            .placeholder(R.color.place_holder_color)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    val newBitmap = Helper.reduceImageBitmap(resource, 2048)
                    binding.slideView.setDrawableLeft(BitmapDrawable(newBitmap))
                    val w = newBitmap.width
                    val h = newBitmap.height
                    val ratio = h.toFloat().div(w)
                    val layoutParams = binding.cardViewBeforeAfter.layoutParams as ConstraintLayout.LayoutParams
                    val dimensionRatio = "1:$ratio"
                    Log.d("asd123", "dimensionRatio = $dimensionRatio")
                    layoutParams.dimensionRatio = dimensionRatio
                    binding.cardViewBeforeAfter.layoutParams = layoutParams
                    Log.d(
                        "asd123",
                        "loadImage - onResourceReady, imageSize = ${newBitmap.width} - ${newBitmap.height}"
                    )
                    uploadAndUpscaleImage(newBitmap)
                    isDisplayingImageBefore = true
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    Log.d("asd123", "onLoadCleared")
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    Log.d("asd123", "onLoadFailed")
                    super.onLoadFailed(errorDrawable)
                }
            })
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
                    binding.slideView.setDrawableRight(BitmapDrawable(resource))
                    viewModel.setOutputImage(resource)
                    isDisplayingImageBefore = false
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
                            this@EnhancerResultActivity,
                            "Got error from server! Please try again!"
                        )
                        dismissLoadingDialog()
                    }
                }

            })
    }

    private fun uploadAndUpscaleImage(imageBitmap: Bitmap) {
        val currentTime = System.currentTimeMillis()
        val dateTime = Helper.convertMillisToDateTime(currentTime)
        val name = dateTime + "-" + Random.nextInt(1000, 9999).toString()
        val storeRef = FirebaseStorage.getInstance().getReference("images/$name")

        storeRef.putBytes(imageBitmap.convertToByteArray())
            .addOnSuccessListener {
                Log.d("asd123", "upload success")
                storeRef.downloadUrl.addOnSuccessListener {
                    Log.d("asd123", "downloadUrl success, url = $it")
                    viewModel.upscaleImage(it.toString(), computeScaleValue(imageBitmap))
                }.addOnFailureListener {
                    Log.d("asd123", "downloadUrl fail")
                }
            }
            .addOnFailureListener {
                Log.d("asd123", "upload failed, $it, ${it.message}, ${it.printStackTrace()}")
            }
    }

    private fun computeScaleValue(imageBitmap: Bitmap): Int {
        val width = imageBitmap.width
        val height = imageBitmap.height
        val max = max(width, height)
        return when {
            max < 1000 -> 4
            max < 1500 -> 3
            else -> 2
        }
    }

    private fun showLoadingDialog() {
        if (loadingDialog != null) {
            loadingDialog?.show()
            return
        }

        loadingDialog = UiHelper.createLoadingDialog(this)
        loadingDialog?.show()
//        loadingDialog = Dialog(this)
//        loadingDialog?.apply {
//            requestWindowFeature(Window.FEATURE_NO_TITLE)
//            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//            setCancelable(false)
//        }
//        val dialogBinding = DialogBasicLoadingBinding.inflate(LayoutInflater.from(this))
//        loadingDialog?.setContentView(dialogBinding.root)
//        loadingDialog?.show()
//        dialogBinding.apply {
//            lottieView.playAnimation()
//        }
    }

    private fun dismissLoadingDialog() {
        loadingDialog?.dismiss()
    }

    override fun onDestroy() {
        loadingDialog?.dismiss()
        loadingDialog = null
        super.onDestroy()
    }

    private fun observeViewModel() {
        viewModel.upscaledImageUrl.observe(this) { imageUrl ->
            loadResultImage(imageUrl)
        }
        viewModel.isServerError.observe(this) {
            dismissLoadingDialog()
            UiHelper.showShortToast(this, "Got error from server!")
        }
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