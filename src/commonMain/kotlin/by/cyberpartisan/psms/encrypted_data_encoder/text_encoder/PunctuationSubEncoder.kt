package by.cyberpartisan.psms.encrypted_data_encoder.text_encoder

import com.ionspin.kotlin.bignum.integer.BigInteger

class PunctuationSubEncoder : SubEncoder {
    override fun encode(currentValue: BigInteger, remainingSize: BigInteger): EncodeResult {
        return if (currentValue == BigInteger.ZERO) {
            when {
                remainingSize <= BigInteger(1) -> {
                    EncodeResult(BigInteger(0), ".", needSpaceBefore = false)
                }
                remainingSize <= BigInteger(8) -> {
                    EncodeResult(BigInteger(8), "?", needSpaceBefore = false)
                }
                else -> {
                    EncodeResult(BigInteger(8 * 8), "!", needSpaceBefore = false)
                }
            }
        } else {
            when ((currentValue % 4).intValue()) {
                0 -> EncodeResult(BigInteger(8), ",", needSpaceBefore = false)
                1 -> EncodeResult(BigInteger(8), ":", needSpaceBefore = false)
                2 -> EncodeResult(BigInteger(8), "(", needSpaceBefore = false)
                3 -> EncodeResult(BigInteger(8), ")", needSpaceBefore = false)
                else -> throw Exception()
            }
        }
    }

}
