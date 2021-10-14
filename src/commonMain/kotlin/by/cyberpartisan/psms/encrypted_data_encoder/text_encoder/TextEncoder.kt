package by.cyberpartisan.psms.encrypted_data_encoder.text_encoder

import by.cyberpartisan.psms.encrypted_data_encoder.EncryptedDataEncoder
import com.ionspin.kotlin.bignum.integer.BigInteger
import com.ionspin.kotlin.bignum.integer.Sign

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

    override fun encode(data: ByteArray): String {
        val targetBitCount = BigInteger.ONE shl ((data.size + 1) * 8)
        var currentBitCount = BigInteger.ONE
        var currentValue = BigInteger.fromByteArray(data.reversedArray(), Sign.POSITIVE)
        val words = ArrayList<EncodeResult>()
        while (currentBitCount < targetBitCount) {
            // needSpaceBefore is checked to avoid "word)(,. word"
            val subEncoderList = if (words.isNotEmpty() && words.last().needSpaceBefore && words.last().needSpaceAfter) subEncoders else spacedSubEncoders
            val subEncoder = subEncoderList[(currentValue % subEncoderList.size).intValue()]
            currentValue /= subEncoderList.size
            currentBitCount *= subEncoderList.size
            val result = subEncoder.encode(currentValue)
            words.add(result)
            if (result.size != BigInteger.ZERO) {
                currentValue /= result.size
                currentBitCount *= result.size
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
        TODO()
    }
}