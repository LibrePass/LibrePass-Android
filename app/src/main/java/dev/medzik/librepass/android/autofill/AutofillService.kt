package dev.medzik.librepass.android.autofill

import android.os.Build
import android.os.CancellationSignal
import android.service.autofill.AutofillService
import android.service.autofill.FillCallback
import android.service.autofill.FillRequest
import android.service.autofill.SaveCallback
import android.service.autofill.SaveRequest
import androidx.annotation.RequiresApi
import dagger.hilt.android.AndroidEntryPoint
import dev.medzik.librepass.android.utils.Vault
import javax.inject.Inject

@AndroidEntryPoint
@RequiresApi(Build.VERSION_CODES.O)
class LibrePassAutofillService : AutofillService() {
    @Inject
    lateinit var vault: Vault

    override fun onFillRequest(
        request: FillRequest,
        cancellationSignal: CancellationSignal,
        callback: FillCallback
    ) {
        AutofillHandler.handleAutofill(
            vault,
            context = this,
            request,
            cancellationSignal,
            callback
        )
    }

    override fun onSaveRequest(
        request: SaveRequest,
        callback: SaveCallback
    ) {
        // TODO: implement
    }
}
