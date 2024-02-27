package dev.medzik.librepass.android.utils

import android.content.Context
import dev.medzik.android.utils.runOnUiThread
import dev.medzik.android.utils.showToast
import dev.medzik.librepass.android.BuildConfig
import dev.medzik.librepass.android.R
import dev.medzik.librepass.client.errors.ApiException
import dev.medzik.librepass.client.errors.ClientException
import dev.medzik.librepass.errors.ServerError

/** Log exception if debugging is enabled. */
fun Exception.debugLog() {
    if (BuildConfig.DEBUG) {
        printStackTrace()
    }
}

/** Handle exceptions. Show toast with an error message. */
fun Exception.showErrorToast(context: Context) {
    // log exception trace if debugging is enabled
    debugLog()

    val message =
        when (this) {
//        // handle encrypt exception
//        is EncryptException -> { context.getString(R.string.Error_EncryptionError) }
            // handle client exception (network error)
            is ClientException -> {
                context.getString(R.string.Error_NetworkError)
            }
            // handle api exceptions
            is ApiException -> {
                getTranslatedErrorMessage(context)
            }
            // handle other exceptions
            else -> {
                context.getString(R.string.Error_UnknownError)
            }
        }

    runOnUiThread { context.showToast(message) }
}

fun ApiException.getTranslatedErrorMessage(context: Context): String {
    return when (getServerError()) {
//        ServerError.CipherNotFound -> context.getString(R.string.CipherNotFound)
//        ServerError.CollectionNotFound -> context.getString(R.string.CollectionNotFound)
        ServerError.Database -> context.getString(R.string.Database)
        ServerError.Duplicated -> context.getString(R.string.Duplicated)
        ServerError.EmailNotVerified -> context.getString(R.string.EmailNotVerified)
        ServerError.InvalidBody -> context.getString(R.string.InvalidBody)
        ServerError.InvalidSharedSecret -> context.getString(R.string.InvalidCredentials)
        ServerError.InvalidToken -> context.getString(R.string.InvalidToken)
//        ServerError.InvalidTwoFactor -> context.getString(R.string.InvalidTwoFactor)
//        ServerError.NotFound -> context.getString(R.string.NotFound)
        ServerError.RateLimit -> context.getString(R.string.RateLimit)

        else -> message
    }
}
