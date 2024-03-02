package dev.medzik.librepass.android.utils

import android.content.Context
import android.net.ConnectivityManager

fun Context.haveNetworkConnection(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork = connectivityManager.activeNetwork
    return activeNetwork != null
}
