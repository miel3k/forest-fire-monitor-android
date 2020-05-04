package com.tp.base.extension

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment

fun Context.toast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun Context.toast(@StringRes resId: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, this.resources.getText(resId), duration).show()
}

fun Fragment.toast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    context?.toast(message, duration)
}

fun Fragment.toast(@StringRes resId: Int, duration: Int = Toast.LENGTH_SHORT) {
    context?.toast(resId, duration)
}