package dev.medzik.librepass.android.utils.exception

import android.content.Context
import dev.medzik.librepass.android.R
import dev.medzik.librepass.client.errors.ApiException
import dev.medzik.librepass.responses.ResponseError

fun ApiException.getTranslatedErrorMessage(context: Context): String {
    return when (getResponseError()) {
        ResponseError.INVALID_CREDENTIALS ->
            context.getString(R.string.API_Error_INVALID_CREDENTIALS)
        ResponseError.RE_LOGIN_REQUIRED ->
            context.getString(R.string.API_Error_RE_LOGIN_REQUIRED)
        ResponseError.EMAIL_NOT_VERIFIED ->
            context.getString(R.string.API_Error_EMAIL_NOT_VERIFIED)
        ResponseError.TOO_MANY_REQUESTS ->
            context.getString(R.string.API_Error_TOO_MANY_REQUESTS)
        ResponseError.DATABASE_DUPLICATED_KEY ->
            context.getString(R.string.API_Error_DATABASE_DUPLICATED_KEY)
        ResponseError.UNEXPECTED_SERVER_ERROR ->
            context.getString(R.string.API_Error_UNEXPECTED_SERVER_ERROR)
        else -> message
    }
}
