package dev.medzik.librepass.android.common.parceler

import android.os.Parcel
import dev.medzik.librepass.types.cipher.CipherType
import kotlinx.parcelize.Parceler

object CipherTypeParceler : Parceler<CipherType> {
    override fun create(parcel: Parcel) = CipherType.from(parcel.readInt())

    override fun CipherType.write(parcel: Parcel, flags: Int) {
        parcel.writeInt(ordinal)
    }
}
