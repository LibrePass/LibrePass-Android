package dev.medzik.librepass.android.utils

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@OptIn(DelicateCoroutinesApi::class)
object Toast {
    fun Context.showToast(text: String) {
        val context = this
        GlobalScope.launch(Dispatchers.Main) { Toast.makeText(context, text, Toast.LENGTH_LONG).show() }
    }

    fun Context.showToast(@StringRes resId: Int) {
        val context = this
        GlobalScope.launch(Dispatchers.Main) { Toast.makeText(context, resId, Toast.LENGTH_LONG).show() }
    }
}
