package dev.medzik.android.utils

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes

/**
 * Show toast dialog.
 * @param text The text to show.
 */
fun Context.showToast(text: String) =
    runOnUiThread {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show()
    }

/**
 * Show toast dialog.
 * @param resId The resource id of the string resource to use.
 */
fun Context.showToast(
    @StringRes resId: Int
) = runOnUiThread {
    Toast.makeText(this, resId, Toast.LENGTH_LONG).show()
}
