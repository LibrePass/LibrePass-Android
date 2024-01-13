package dev.medzik.librepass.android.autofill

import android.os.Build
import android.os.CancellationSignal
import android.service.autofill.AutofillService
import android.service.autofill.FillCallback
import android.service.autofill.FillRequest
import android.service.autofill.SaveCallback
import android.service.autofill.SaveRequest
import androidx.annotation.RequiresApi
import dev.medzik.librepass.android.utils.Vault

@RequiresApi(Build.VERSION_CODES.O)
class LibrePassAutofillService : AutofillService() {
//    @Inject
    var vault: Vault = Vault()

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
