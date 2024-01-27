package dev.medzik.librepass.android.autofill

import android.app.assist.AssistStructure
import android.os.Build
import android.os.CancellationSignal
import android.service.autofill.FillCallback
import android.service.autofill.FillRequest
import android.service.autofill.FillResponse
import android.util.Log
import androidx.annotation.RequiresApi
import dev.medzik.android.autofill.AutofillUtils.getAssistInfo
import dev.medzik.android.autofill.AutofillUtils.getWindowNodes
import dev.medzik.librepass.android.utils.Vault
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
object AutofillHandler {
    private const val TAG = "AutofillHandler"

    fun handleAutofill(
        vault: Vault,
        request: FillRequest,
        cancellationSignal: CancellationSignal,
        callback: FillCallback
    ) {
        val windowNode = getWindowNodes(request.fillContexts).lastOrNull()
        if (windowNode?.rootViewNode == null) {
            Log.i(TAG, "No window node found")
            return callback.onSuccess(null)
        }

        val exceptionHandler =
            CoroutineExceptionHandler { _, exception ->
                Log.e(TAG, exception.toString())
                callback.onSuccess(null)
            }
        val job =
            CoroutineScope(Dispatchers.IO)
                .launch(exceptionHandler) {
                    searchAndFill(vault, windowNode, request, callback)
                }

        cancellationSignal.setOnCancelListener {
            Log.d(TAG, "Cancelling autofill")
            job.cancel()
        }
    }

    private suspend fun searchAndFill(
        vault: Vault,
        windowNode: AssistStructure.WindowNode,
        request: FillRequest,
        callback: FillCallback
    ) {
        val assistInfo = getAssistInfo(windowNode) ?: return

        if (vault.aesKey.isEmpty()) {
            // TODO: unlock vault
            Log.w(TAG, "Vault is locked")
            return
        }

        val responseBuilder = FillResponse.Builder()

        // TODO: build response

        if (!currentCoroutineContext().isActive) {
            Log.d(TAG, "Job was cancelled")
            callback.onSuccess(null)
        } else {
            callback.onSuccess(responseBuilder.build())
        }
    }
}
