package kr.co.kit.kitdist.utils

import androidx.annotation.IntRange
import okhttp3.internal.and

fun ByteArray.toHexString(): String = joinToString("") {
    (0xFF and it.toInt()).toString(16).padStart(2, '0')
}

fun ByteArray.convertToInt(): Int {
    if (size > 2) throw IllegalStateException("Out of length. Not over 2.")
    var result = 0
    forEach { result = result shl 8 or (it and 0x000000FF) }
    return result
}

fun Byte.convertToInt(): Int = byteArrayOf(0x00.toByte(), this).convertToInt()

fun Int.convertToBytes(
    @IntRange(from = 1, to = 4) length: Int
): ByteArray = byteArrayOf(
    (this ushr 24).toByte(),
    (this ushr 16).toByte(),
    (this ushr 8).toByte(),
    this.toByte()
).run { copyOfRange(this.size - length, this.size) }

fun ByteArray.showHexData(): String =
    toHexString().chunkedSequence(2).joinToString(" ")