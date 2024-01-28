package com.zeroapp.bloodpresshelp.kotlintoastlibrary

import android.content.Context
import android.widget.Toast

object KotlinToastHelper {
    fun showShortToast(context: Context, msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }
}