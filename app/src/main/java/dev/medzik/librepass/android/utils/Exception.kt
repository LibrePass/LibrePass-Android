package dev.medzik.librepass.android.utils

import android.content.Context
import dev.medzik.android.utils.runOnUiThread
import dev.medzik.android.utils.showToast
import dev.medzik.librepass.android.BuildConfig
import dev.medzik.librepass.android.R
import dev.medzik.librepass.client.errors.ApiException
import dev.medzik.librepass.client.errors.ClientException
import dev.medzik.librepass.responses.ResponseError

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
    return when (responseError) {
        ResponseError.INVALID_CREDENTIALS -> context.getString(R.string.API_Error_INVALID_CREDENTIALS)
        ResponseError.RE_LOGIN_REQUIRED -> context.getString(R.string.API_Error_RE_LOGIN_REQUIRED)
        ResponseError.EMAIL_NOT_VERIFIED -> context.getString(R.string.API_Error_EMAIL_NOT_VERIFIED)
        ResponseError.TOO_MANY_REQUESTS -> context.getString(R.string.API_Error_TOO_MANY_REQUESTS)
        ResponseError.DATABASE_DUPLICATED_KEY -> context.getString(R.string.API_Error_DATABASE_DUPLICATED_KEY)
        ResponseError.UNEXPECTED_SERVER_ERROR -> context.getString(R.string.API_Error_UNEXPECTED_SERVER_ERROR)
        ResponseError.CIPHER_TOO_LARGE -> context.getString(R.string.API_Error_CIPHER_TOO_LARGE)
        else -> message
    }
}
