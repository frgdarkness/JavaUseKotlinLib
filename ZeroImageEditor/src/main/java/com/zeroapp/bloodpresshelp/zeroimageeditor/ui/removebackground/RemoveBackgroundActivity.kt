package com.zeroapp.bloodpresshelp.zeroimageeditor.ui.removebackground

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.zeroapp.bloodpresshelp.zeroimageeditor.Helper
import com.zeroapp.bloodpresshelp.zeroimageeditor.R
import com.zeroapp.bloodpresshelp.zeroimageeditor.RemoteConfig
import com.zeroapp.bloodpresshelp.zeroimageeditor.databinding.ActivityRemoveBackgroundBinding
import com.zeroapp.bloodpresshelp.zeroimageeditor.ui.UiHelper.gone
import com.zeroapp.bloodpresshelp.zeroimageeditor.ui.UiHelper.show

class RemoveBackgroundActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityRemoveBackgroundBinding
    private lateinit var viewModel: RemoveBackgroundViewModel

    companion object {
        private const val GET_IMAGE_TO_ENHANCER_REQUEST_CODE = 444
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.statusBarColor = ContextCompat.getColor(this, R.color.white)
        binding = ActivityRemoveBackgroundBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this)[RemoveBackgroundViewModel::class.java]
        setContentView(binding.root)
        initView()
        initEvent()
    }

    private fun initView() {
        binding.apply {
            layoutActionBar.tvTitle.text = "Remove BG"
            layoutActionBar.cardViewSave.gone()
        }
    }

    private fun initEvent() {
        binding.apply {
            cardViewPickImage.setOnClickListener {
                requestSelectImage()
            }
            btnRemoveBg.setOnClickListener {
                val imageUri = viewModel.getImageUri()
                if (imageUri != null) {
                    val intent = Intent(this@RemoveBackgroundActivity, RemoveBackgroundResultActivity::class.java)
                    intent.putExtra("IMAGE_URI", imageUri)
                    startActivity(intent)
                }
            }
            layoutActionBar.btnMenu.setOnClickListener {
                onBackPressed()
            }
            layoutActionBar.cardViewSave.gone()
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
        updateStateCardViewRemoveBg()
        binding.apply {
            tvChangeImage.show()
            Glide.with(this@RemoveBackgroundActivity)
                .asBitmap()
                .load(imageUri)
                .placeholder(R.color.place_holder_color)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        val newBitmap = Helper.reduceImageBitmap(resource, RemoteConfig.MAX_SIZE_SELECTED_IMAGE)
                        imageUpload.setImageBitmap(newBitmap)
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

    private fun updateStateCardViewRemoveBg() {
        binding.apply {
            val uri = viewModel.getImageUri()
            Log.d("asd123", "updateStateGenerateCardView, uri = $uri")
            if (uri != null) {
//                btnEnhancer.iconTint = ContextCompat.getColorStateList(this@EnhancerActivity, R.color.color_vip)
                btnRemoveBg.setBackgroundColor(ContextCompat.getColor(this@RemoveBackgroundActivity, android.R.color.black))
            } else {
//                btnEnhancer.iconTint = ContextCompat.getColorStateList(this@EnhancerActivity, R.color.cancel_button_color)
                btnRemoveBg.setBackgroundColor(Color.parseColor("#D0D0D0"))
            }
        }
    }
}
