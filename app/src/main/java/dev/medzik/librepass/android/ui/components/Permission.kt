package dev.medzik.librepass.android.ui.components

import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import dev.medzik.android.components.rememberMutableBoolean

@Composable
fun Permission(
    permission: String,
    onDenied: @Composable (requester: @Composable () -> Unit) -> Unit,
    onGranted: @Composable () -> Unit = {}
) {
    val ctx = LocalContext.current

    // check the initial state of permission, it may be already granted
    var grantState by rememberMutableBoolean(
        ContextCompat.checkSelfPermission(
            ctx,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    )

    if (grantState) {
        onGranted()
    } else {
        val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) {
            grantState = it
        }

        onDenied { SideEffect { launcher.launch(permission) } }
    }
}
