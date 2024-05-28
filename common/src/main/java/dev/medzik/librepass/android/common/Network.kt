package dev.medzik.librepass.android.common

import android.content.Context
import android.net.ConnectivityManager

fun Context.haveNetworkConnection(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork = connectivityManager.activeNetwork
    return activeNetwork != null
}
