package dev.medzik.librepass.android.ui.components

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.journeyapps.barcodescanner.CaptureManager
import com.journeyapps.barcodescanner.DecoratedBarcodeView

@Composable
fun QrCodeScanner(onScanned: (String) -> Unit) {
    val context = LocalContext.current

    val compoundBarcodeView =
        remember {
            DecoratedBarcodeView(context).apply {
                val capture = CaptureManager(context as Activity, this)
                capture.initializeFromIntent(context.intent, null)
                this.setStatusText("")
                this.resume()
                capture.decode()
                this.decodeContinuous { result ->
                    result.text?.let { scannedText ->
                        Log.i("QR_SCANNER", scannedText)
                        onScanned.invoke(scannedText)
                    }
                }
            }
        }

    AndroidView(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(300.dp),
        factory = { compoundBarcodeView },
    )
}
