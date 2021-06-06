package by.cyberpartisan.psms.encrypted_data_encoder.text_encoder

import by.cyberpartisan.psms.encrypted_data_encoder.EncryptedDataEncoder
import com.ionspin.kotlin.bignum.integer.BigInteger
import com.ionspin.kotlin.bignum.integer.Sign

class TextEncoder(
    nonSpacesSubEncoders: List<SubEncoder>,
    private val spacedSubEncoders: List<SubEncoder>) : EncryptedDataEncoder {

    private val subEncoders = nonSpacesSubEncoders + spacedSubEncoders

    constructor() : this(listOf(PunctuationSubEncoder()),
        listOf(DateTimeSubEncoder()) +
                WordsSubEncoderInstances.instances +
                WordsSubEncoderInstances9.instances +
                WordsSubEncoderInstances10.instances) {
    }

    override fun encode(data: ByteArray): String {
        val targetSize = BigInteger.ONE shl data.size * 8
        var currentSize = BigInteger.ONE
        var currentValue = BigInteger.fromByteArray(data, Sign.POSITIVE)
        val words = ArrayList<EncodeResult>()
        while (currentSize < targetSize) {
            val subEncoderList = if (words.isNotEmpty() && words.last().needSpaceBefore && words.last().needSpaceAfter) subEncoders else spacedSubEncoders
            val subEncoder = subEncoderList[(currentValue % subEncoderList.size).intValue()]
            currentValue /= subEncoderList.size
            currentSize *= subEncoderList.size
            val remainingSize = targetSize / currentSize + if (targetSize % currentSize > 0) 1 else 0
            val result = subEncoder.encode(currentValue, remainingSize)
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
        TODO()
    }
}