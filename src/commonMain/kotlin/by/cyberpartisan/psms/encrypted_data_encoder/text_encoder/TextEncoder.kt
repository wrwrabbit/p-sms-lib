package by.cyberpartisan.psms.encrypted_data_encoder.text_encoder

import by.cyberpartisan.psms.encrypted_data_encoder.EncryptedDataEncoder
import com.ionspin.kotlin.bignum.integer.BigInteger
import com.ionspin.kotlin.bignum.integer.Sign
import kotlin.math.ceil
import kotlin.random.Random

class TextEncoder(
    nonSpacesSubEncoders: List<SubEncoder>,
    private val spacedSubEncoders: List<SubEncoder>) : EncryptedDataEncoder {

    private val subEncoders = nonSpacesSubEncoders + spacedSubEncoders

    private constructor() : this(listOf(PunctuationSubEncoder()),
        listOf(DateTimeSubEncoder()) +
                WordsSubEncoderInstances.instances +
                WordsSubEncoderInstances9.instances +
                WordsSubEncoderInstances10.instances) {
    }

    companion object {
        val instance: TextEncoder by lazy {
            TextEncoder()
        }
    }

    private class RequirePadding: Exception() {}

    override fun hasFrontPadding(): Boolean = true

    override fun encode(data: ByteArray): String {
        var dataCopy = data
        var actualData = dataCopy
        var bytesList = mutableListOf<Byte>()
        while (true) {
            try {
                return actualEncode(actualData)
            } catch (_: RequirePadding) {
                if (bytesList.isEmpty()) {
                    dataCopy = actualData
                    bytesList = (0..255).toList().map { it.toByte() }.shuffled().toMutableList()
                }
                actualData = byteArrayOf(bytesList.last()) + dataCopy
                bytesList.removeLast()
            }
        }
    }

    private fun actualEncode(data: ByteArray): String {
        val targetSize = BigInteger.ONE shl (data.size * 8)
        var currentSize = BigInteger.ONE
        var currentValue = BigInteger.fromByteArray(data, Sign.POSITIVE)
        val words = ArrayList<EncodeResult>()
        while (currentSize < targetSize) {
            // needSpaceBefore is checked to avoid "word)(,. word"
            val subEncoderList = if (words.isNotEmpty() && words.last().needSpaceBefore && words.last().needSpaceAfter) subEncoders else spacedSubEncoders
            val subEncoder = subEncoderList[(currentValue % subEncoderList.size).intValue()]
            if (currentValue < subEncoderList.size) {
                throw RequirePadding()
            }
            currentValue /= subEncoderList.size
            currentSize *= subEncoderList.size
            val result = subEncoder.encode(currentValue)
            words.add(result)
            if (result.size != BigInteger.ZERO) {
                currentValue /= result.size
                currentSize *= result.size
            }
        }
        var result = ""
        var needSpaceAfterPrevious = true
        for (word in words) {
            if (result.isNotEmpty() && word.needSpaceBefore && needSpaceAfterPrevious) {
                result += " "
            }
            result += word.word
            needSpaceAfterPrevious = word.needSpaceAfter
        }
        return result
    }

    override fun decode(str: String): ByteArray {
        var index = 0
        var needSpace = true
        var actualSize = BigInteger(0)
        val coefficients = ArrayList<Pair<Int, Int>>()
        while (index < str.length) {
            val subEncoderList = if (!needSpace) subEncoders else spacedSubEncoders
            var decodeResult: DecodeResult? = null
            for (i in subEncoderList.indices) {
                decodeResult = subEncoderList[i].decode(str, index)
                if (decodeResult != null) {
                    coefficients.add(Pair(i, subEncoderList.size))
                    coefficients.add(Pair(decodeResult.value, decodeResult.size))
                    needSpace = !decodeResult.needSpaceBefore || !decodeResult.needSpaceAfter
                    index = decodeResult.newPosition
                    actualSize += decodeResult.size + subEncoderList.size
                    break
                }
            }
            if (decodeResult == null) {
                throw IllegalArgumentException("'${str}' at $index is not in valid Text scheme")
            }
        }
        val resultBytes = coefficients.reversed().fold(BigInteger(0)){ acc, pair -> acc * pair.second + pair.first }.toByteArray()
        return ByteArray(sizeToByteCount(actualSize)) + resultBytes
    }

    private fun sizeToByteCount(size: BigInteger) : Int {
        var sizeCopy = size
        var bitIndex = 0
        var hasNonHighestBits = false
        while (sizeCopy != BigInteger(0)) {
            hasNonHighestBits = hasNonHighestBits || (sizeCopy and BigInteger(1) == BigInteger(1))
            sizeCopy = sizeCopy shr 1
            bitIndex++
        }
        val bitCount = if (hasNonHighestBits) bitIndex + 1 else bitIndex
        return ceil(bitCount.toDouble() / 8).toInt()
    }
}