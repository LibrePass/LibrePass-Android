package dev.medzik.librepass.android.utils

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes

object Toast {
    fun Context.showToast(@StringRes resId: Int) =
        Toast.makeText(this, resId, Toast.LENGTH_LONG).show()
}
