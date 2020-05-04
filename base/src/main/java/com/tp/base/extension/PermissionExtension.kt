package com.tp.base.extension

import android.app.Activity
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener

fun Activity.requestPermission(
    vararg permissions: String,
    onGranted: () -> Unit,
    onDenied: () -> Unit
) {
    val listener = object : MultiplePermissionsListener {
        override fun onPermissionsChecked(report: MultiplePermissionsReport) {
            if (report.deniedPermissionResponses.isNullOrEmpty()) {
                onGranted()
            } else {
                onDenied()
            }
        }

        override fun onPermissionRationaleShouldBeShown(
            permissions: MutableList<PermissionRequest>?,
            token: PermissionToken?
        ) {
            token?.continuePermissionRequest()
        }
    }
    Dexter.withActivity(this).withPermissions(*permissions).withListener(listener).check()
}