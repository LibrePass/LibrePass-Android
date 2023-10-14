package dev.medzik.librepass.android.utils.exception

import android.content.Context
import dev.medzik.android.utils.showToast
import dev.medzik.librepass.android.BuildConfig
import dev.medzik.librepass.android.R
import dev.medzik.librepass.client.errors.ApiException
import dev.medzik.librepass.client.errors.ClientException
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Log exception if debugging is enabled.
 */
fun Exception.debugLog() {
    if (BuildConfig.DEBUG) {
        printStackTrace()
    }
}

/**
 * Handle exceptions. Show toast with an error message.
 * @param context The context to get string resources
 */
@OptIn(DelicateCoroutinesApi::class)
fun Exception.handle(context: Context) {
    // log exception trace if debugging is enabled
    debugLog()

    when (this) {
        // handle encrypt exception
        is EncryptException -> {
            GlobalScope.launch {
                context.showToast(R.string.Error_EncryptionError)
            }
        }

        // handle client exception (network error)
        is ClientException -> {
            GlobalScope.launch {
                context.showToast(R.string.Error_NetworkError)
            }
        }

        // handle api exceptions
        is ApiException -> {
            GlobalScope.launch {
                context.showToast(getTranslatedErrorMessage(context))
            }
        }

        // handle other exceptions
        else -> {
            GlobalScope.launch {
                context.showToast(R.string.Error_UnknownError)
            }
        }
    }
}

class EncryptException(override val message: String) : Exception()
