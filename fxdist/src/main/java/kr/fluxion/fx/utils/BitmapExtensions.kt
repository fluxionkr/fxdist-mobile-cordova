package kr.fluxion.fx.utils

import android.graphics.Bitmap
import android.util.Base64
import java.io.ByteArrayOutputStream

private const val PREFIX_BASE64 = "data:image/jpeg;base64,"

fun Bitmap.getBase64() =
    "$PREFIX_BASE64${Base64.encodeToString(getBitmapBytes(this), Base64.DEFAULT)}"

private fun getBitmapBytes(bitmap: Bitmap): ByteArray = ByteArrayOutputStream().apply {
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, this)
}.toByteArray()