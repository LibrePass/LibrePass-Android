package dev.medzik.librepass.android.activity

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity

@RequiresApi(api = Build.VERSION_CODES.O)
class AutofillLauncherActivity : AppCompatActivity() {
    companion object {
        private val TAG = AutofillLauncherActivity::class.java.name

        fun getPendingIntent(context: Context): PendingIntent? {
            try {
                return PendingIntent.getActivity(
                    context,
                    0,
                    Intent(context, AutofillLauncherActivity::class.java),
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
                    } else {
                        PendingIntent.FLAG_CANCEL_CURRENT
                    }
                )
            } catch (e: RuntimeException) {
                Log.e(TAG, "Unable to create pending intent for selection", e)
                return null
            }
        }
    }
}
