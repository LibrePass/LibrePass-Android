package dev.medzik.android.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri

fun Activity.openEmailApplication(
    email: String,
    subject: String,
    body: String
) {
    val intent =
        Intent(
            Intent.ACTION_SENDTO,
            Uri.parse("mailto:$email")
        )
    intent.putExtra(Intent.EXTRA_SUBJECT, subject)
    intent.putExtra(Intent.EXTRA_TEXT, body)
    startActivity(Intent.createChooser(intent, "Email"))
}
