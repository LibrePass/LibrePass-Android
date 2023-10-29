@file:Suppress("UNUSED")

package dev.medzik.android.utils

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes

/** Shows the toast dialog. */
fun Context.showToast(text: String) =
    runOnUiThread {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show()
    }

/** Shows the toast dialog. */
fun Context.showToast(
    @StringRes resId: Int
) = runOnUiThread {
    Toast.makeText(this, resId, Toast.LENGTH_LONG).show()
}
