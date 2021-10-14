package by.cyberpartisan.psms.encrypted_data_encoder.text_encoder

import com.ionspin.kotlin.bignum.integer.BigInteger

const val chars = ",:().?!"

class PunctuationSubEncoder : SubEncoder {
    override fun encode(currentValue: BigInteger): EncodeResult {
        return when ((currentValue % chars.length).intValue()) {
            0 -> EncodeResult(BigInteger(chars.length), ",", needSpaceBefore = false)
            1 -> EncodeResult(BigInteger(chars.length), ":", needSpaceBefore = false)
            2 -> EncodeResult(BigInteger(chars.length), "(", needSpaceBefore = false)
            3 -> EncodeResult(BigInteger(chars.length), ")", needSpaceBefore = false)
            4 -> EncodeResult(BigInteger(chars.length), ".", needSpaceBefore = false)
            5 -> EncodeResult(BigInteger(chars.length), "?", needSpaceBefore = false)
            6 -> EncodeResult(BigInteger(chars.length), "!", needSpaceBefore = false)
            else -> throw Exception()
        }
    }

    override fun decode(str: String, index: Int): DecodeResult? {
        val charIndex = chars.indexOf(str[index])
        return if (charIndex != -1) DecodeResult(BigInteger(chars.length), charIndex, index + 1) else null
    }
}
