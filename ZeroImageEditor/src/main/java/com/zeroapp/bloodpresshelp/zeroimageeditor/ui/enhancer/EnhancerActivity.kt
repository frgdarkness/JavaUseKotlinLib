package com.zeroapp.bloodpresshelp.zeroimageeditor.ui.enhancer

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.zeroai.photoeditorai.ui.enhancer.EnhancerViewModel
import com.zeroapp.bloodpresshelp.zeroimageeditor.Constance
import com.zeroapp.bloodpresshelp.zeroimageeditor.Helper
import com.zeroapp.bloodpresshelp.zeroimageeditor.R
import com.zeroapp.bloodpresshelp.zeroimageeditor.data.remote.model.ImageType
import com.zeroapp.bloodpresshelp.zeroimageeditor.databinding.ActivityEnhancerBinding
import com.zeroapp.bloodpresshelp.zeroimageeditor.ui.UiHelper.gone
import com.zeroapp.bloodpresshelp.zeroimageeditor.ui.UiHelper.show

class EnhancerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEnhancerBinding
    private lateinit var viewModel: EnhancerViewModel
    private lateinit var remoteConfig: FirebaseRemoteConfig

    companion object {
        private const val GET_IMAGE_TO_ENHANCER_REQUEST_CODE = 333
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        window?.statusBarColor = ContextCompat.getColor(this, R.color.white)
        binding = ActivityEnhancerBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this)[EnhancerViewModel::class.java]
        doLoadConfig()
        setContentView(binding.root)
        initView()
        initEvent()
    }

    private fun connectFirebase() {
        val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
    }

    private fun doLoadConfig() {
        loadConfig {
            val enable = remoteConfig.getBoolean("enable_upload_image")
            val keyId = remoteConfig.getString("key_id")
            Log.d("asd123", "enable = $enable , keyId = $keyId")
        }
    }

    private fun loadConfig(onLoadFinish: (isSuccess: Boolean) -> Unit) {
        remoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.fetchAndActivate().addOnCompleteListener(this) { task ->

            if (task.isSuccessful) {
                Log.d("asd123", "fetch config success")
                onLoadFinish(true)
            } else {
                Log.d("asd123", "fetch config fail")
                onLoadFinish(false)
            }
        }
    }

    private fun initView() {
        binding.apply {

            layoutActionBar.tvTitle.text = "Enhancer"
            layoutActionBar.cardViewSave.gone()
        }

    }

    private fun initEvent() {
        binding.apply {
            cardViewPickImage.setOnClickListener {
                requestSelectImage()
            }

            btnEnhancer.setOnClickListener {
                val imageUri = viewModel.getImageUri()
                if (imageUri != null) {
                    val intent = Intent(this@EnhancerActivity, EnhancerResultActivity::class.java)
                    intent.putExtra(Constance.IMAGE_URI_DATA, imageUri)
                    intent.putExtra(Constance.IMAGE_TYPE_DATA, viewModel.getImageType().name)
                    intent.putExtra(Constance.SCALE_VALUE_DATA, viewModel.getScale())
                    startActivity(intent)
                }
            }
            binding.layoutActionBar.btnMenu.setOnClickListener {
                onBackPressed()
            }
            radioGroupScale.setOnCheckedChangeListener { _, _ ->
                when (radioGroupScale.checkedRadioButtonId) {
                    btnScale2.id -> viewModel.setScale(2)
                    btnScale3.id -> viewModel.setScale(3)
                    btnScale4.id -> viewModel.setScale(4)
                }
            }
            radioImageType.setOnCheckedChangeListener { _, _ ->
                when (radioImageType.checkedRadioButtonId) {
                    btnAnime.id -> viewModel.setImageType(ImageType.ANIME)
                    btnPhoto.id -> viewModel.setImageType(ImageType.PHOTO)
                }
            }
        }
    }

    private fun requestSelectImage() {
        val intent = Intent()
        intent.apply {
            type = "image/*"
            action = Intent.ACTION_GET_CONTENT
        }
        startActivityForResult(intent, GET_IMAGE_TO_ENHANCER_REQUEST_CODE, null)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d(
            "asd123",
            "onActivityResult, requestCode = $requestCode, resultCode = $resultCode, action = ${data?.action}, data = ${data?.data}"
        )
        val uri = data?.data
        if (requestCode == GET_IMAGE_TO_ENHANCER_REQUEST_CODE && uri != null) {
            onSelectImage(uri)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun onSelectImage(imageUri: Uri) {
        viewModel.setImageUri(imageUri)
        updateStateCardViewEnhancer()
        binding.apply {
            tvChangeImage.show()
            Glide.with(this@EnhancerActivity)
                .asBitmap()
                .load(imageUri)
                .placeholder(R.color.place_holder_color)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        val newBitmap = Helper.reduceImageBitmap(resource, 2048)
                        imageUpload.setImageBitmap(newBitmap)
                        Log.d("asd123", "resourceSize: (${resource.width} - ${resource.height}) -> (${newBitmap.width} - ${newBitmap.height}")
//                        imageUpload.adjustViewBounds = newBitmap.height < newBitmap.width
//                        imageUpload.scaleType = if (newBitmap.height > newBitmap.width) {
//                            ImageView.ScaleType.CENTER_CROP
//                        } else ImageView.ScaleType.FIT_CENTER
//                        val scaleType = imageUpload.scaleType
//                        Log.d("asd123", "scaleType: $scaleType")
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        Log.d("asd123", "onLoadCleared")
                    }

                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        Log.d("asd123", "onLoadFailed")
                        super.onLoadFailed(errorDrawable)

                    }

                })
            imageUpload.show()
        }
    }

    private fun updateStateCardViewEnhancer() {
        binding.apply {
            val uri = viewModel.getImageUri()
            Log.d("asd123", "updateStateGenerateCardView, uri = $uri")
            if (uri != null) {
//                btnEnhancer.iconTint = ContextCompat.getColorStateList(this@EnhancerActivity, R.color.color_vip)
                btnEnhancer.setBackgroundColor(ContextCompat.getColor(this@EnhancerActivity, android.R.color.black))
            } else {
//                btnEnhancer.iconTint = ContextCompat.getColorStateList(this@EnhancerActivity, R.color.cancel_button_color)
                btnEnhancer.setBackgroundColor(Color.parseColor("#D0D0D0"))
            }
        }
    }
}