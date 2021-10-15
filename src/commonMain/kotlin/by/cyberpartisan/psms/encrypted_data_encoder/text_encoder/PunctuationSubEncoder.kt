package by.cyberpartisan.psms.encrypted_data_encoder.text_encoder

import com.ionspin.kotlin.bignum.integer.BigInteger

const val chars = ",:().?!"

class PunctuationSubEncoder : SubEncoder {
    override fun encode(currentValue: BigInteger): EncodeResult {
        val word = chars[(currentValue % chars.length).intValue()].toString()
        return EncodeResult(BigInteger(chars.length), word, needSpaceBefore = false)
    }

    override fun decode(str: String, index: Int): DecodeResult? {
        val charIndex = chars.indexOf(str[index])
        val newPosition = if (index + 1 < str.length && str[index + 1] == ' ') index + 2 else index + 1
        return if (charIndex != -1) DecodeResult(chars.length, charIndex, newPosition, needSpaceBefore = false) else null
    }
}
