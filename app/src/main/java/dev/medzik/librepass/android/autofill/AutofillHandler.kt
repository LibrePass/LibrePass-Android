package dev.medzik.librepass.android.autofill

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.CancellationSignal
import android.service.autofill.FillCallback
import android.service.autofill.FillRequest
import android.util.Log
import androidx.annotation.RequiresApi
import dev.medzik.librepass.android.autofill.Utils.getWindowNodes
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

        val structure = request.fillContexts.last().structure
        StructureParser(structure).parse()?.let {
            // TODO: maybe add blocklist support

            launchSelection(it, vault, context, callback)
        }

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

    @SuppressLint("RemoteViewLayout")
    private fun launchSelection(
        parsedStructure: StructureParser.AutofillResult,
        vault: Vault,
        context: Context,
        callback: FillCallback
    ) {
//        val responseBuilder = FillResponse.Builder()

//        AutofillLauncherActivity.getPendingIntent(context)?.intentSender?.let { intentSender ->
//            val remoteViewUnlock = RemoteViews(
//                context.packageName,
//                R.layout.autofill_unlock
//            )
//
//            remoteViewUnlock.setTextViewText(R.id.text1, "Test")
//
//            responseBuilder.setAuthentication(
//                parsedStructure.getAutofillIDs(),
//                intentSender,
//                remoteViewUnlock
//            )
//        }
//
//        callback.onSuccess(responseBuilder.build())

//        val usernamePresentation = RemoteViews(packageName, R.layout.autofill_unlock)
//        usernamePresentation.setTextViewText(R.id.text1, "my_username")
//
//        val fillResponse = FillResponse.Builder()
//            .addDataset(
//                Dataset.Builder()
//                    .setValue(
//                        parsedStructure.usernameId!!,
//                        AutofillValue.forText("test"),
//                        usernamePresentation
//                    )
//                    .build())
//            .build()
//
//        callback.onSuccess(fillResponse)

//        if (!vault.openedDatabase) {
//
//        }
//
//        parserResult.getAutofillIDs().let { autofillId ->
//            if (autofillId.isNotEmpty()) {
//
//            }
//        }
    }
}
