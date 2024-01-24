package dev.medzik.librepass.android.autofill

import android.content.Context
import android.os.Build
import android.os.CancellationSignal
import android.service.autofill.FillCallback
import android.service.autofill.FillRequest
import android.util.Log
import androidx.annotation.RequiresApi
import dev.medzik.android.autofill.AutofillUtils.getAssistInfo
import dev.medzik.android.autofill.AutofillUtils.getWindowNodes
import dev.medzik.librepass.android.utils.Vault

@RequiresApi(Build.VERSION_CODES.O)
object AutofillHandler {
    private val TAG = AutofillHandler::class.java.name

    fun handleAutofill(
        vault: Vault,
        context: Context,
        request: FillRequest,
        cancellationSignal: CancellationSignal,
        callback: FillCallback
    ) {
        cancellationSignal.setOnCancelListener {
            Log.w(TAG, "Cancelling autofill")
        }

        val windowNode = getWindowNodes(request.fillContexts).lastOrNull()
        if (windowNode?.rootViewNode == null) {
            Log.i(TAG, "No window node found")
            return callback.onSuccess(null)
        }

        val assistInfo = getAssistInfo(windowNode)
        println(assistInfo)

//        val exceptionHandler =
//            CoroutineExceptionHandler { _, exception ->
//                Log.e(TAG, exception.toString())
//                callback.onSuccess(null)
//            }
//        val job =
//            CoroutineScope(Dispatchers.IO)
//                .launch(exceptionHandler) {
//                    // TODO: searchAndFill
//                }
//
//        cancellationSignal.setOnCancelListener {
//            job.cancel()
//        }
    }
}
