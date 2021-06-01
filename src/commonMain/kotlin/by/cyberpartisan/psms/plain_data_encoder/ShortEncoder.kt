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

    /*
    final override fun encode(s: String): ByteArray = merge(s.map { c -> encodeChar(c) }.toByteArray())
    final override fun decode(data: ByteArray): String = String(unMerge(data).map { x -> decodeChar(x) }.toCharArray())

    private fun merge(data: ByteArray): ByteArray {
        var accumulator = BigInteger.ZERO // An initial value of one is required for the case when the first code is zero.
        var firstCodeIsZero = false
        for (code in data.toList()) {
            if (accumulator == BigInteger.ZERO && code.toInt() == 0) {
                firstCodeIsZero = true
                accumulator = BigInteger.ONE
            }
            accumulator = (accumulator shl 7) + code.toUByte().toInt().toBigInteger()
        }
        var result = accumulator.toByteArray()
        if (firstCodeIsZero) {
            if (result[0].toInt() == 1) {
                result = result.sliceArray(1 until result.size)
            } else {
                result[0] = clearMostSignificantBit(result[0])
            }
        }
        return result
    }

    private fun clearMostSignificantBit(value: Byte): Byte {
        var bit = 1
        val intValue = value.toInt() and 0xFF
        while(bit shl 1 <= intValue) {
            bit = bit shl 1
        }
        return (intValue and bit.inv()).toByte()
    }

    private fun unMerge(data: ByteArray): ByteArray {
        var number = BigInteger.fromByteArray(data, Sign.POSITIVE)
        val result = ArrayList<Byte>()
        while (number > BigInteger.ZERO) {
            result.add((number and 0x7F.toBigInteger()).byteValue())
            number = number shr 7
        }
        if (data.isNotEmpty() && data.first() == (0).toByte()) {
            result.add(0)
        }
        result.reverse()
        return result.toByteArray()
    }

    abstract fun encodeChar(char: Char): Byte
    abstract fun decodeChar(code: Byte): Char
     */
}