package dev.medzik.librepass.android.utils

import android.content.Context
import android.widget.Toast

object Toast {
    fun Context.showToast(text: String) =
        Toast.makeText(this, text, Toast.LENGTH_LONG).show()

    fun Context.showToast(resId: Int) =
        Toast.makeText(this, resId, Toast.LENGTH_LONG).show()
}
