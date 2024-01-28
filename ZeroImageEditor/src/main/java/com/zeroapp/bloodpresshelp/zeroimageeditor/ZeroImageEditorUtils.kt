package com.zeroapp.bloodpresshelp.zeroimageeditor

import android.content.Context
import android.content.Intent
import com.zeroapp.bloodpresshelp.zeroimageeditor.ui.enhancer.EnhancerActivity
import com.zeroapp.bloodpresshelp.zeroimageeditor.ui.removebackground.RemoveBackgroundActivity

object ZeroImageEditorUtils {

    fun showEnhancerActivity(context: Context) {
        context.startActivity(Intent(context, EnhancerActivity::class.java))
    }

    fun showRemoveBackgroundActivity(context: Context) {
        context.startActivity(Intent(context, RemoveBackgroundActivity::class.java))
    }
}