package by.cyberpartisan.psms.plain_data_encoder

import com.ionspin.kotlin.bignum.integer.BigInteger
import com.ionspin.kotlin.bignum.integer.Sign
import com.ionspin.kotlin.bignum.integer.toBigInteger

@ExperimentalUnsignedTypes
abstract class ShortEncoder : NotAlignedEncoder() {
    abstract fun decodeChar(code: Int): Char

    var stringBuilder: StringBuilder? = null

    final override fun beforeDecode() {
        stringBuilder = StringBuilder()
    }

    final override fun processDecodingValue(value: Int) {
        stringBuilder!!.append(decodeChar(value))
    }

    final override fun getDecodedString(): String {
        return stringBuilder.toString()
    }

    final override val decodingShifting = 7
}