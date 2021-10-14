package by.cyberpartisan.psms.encrypted_data_encoder.text_encoder

import com.ionspin.kotlin.bignum.integer.BigInteger

interface SubEncoder {
    fun encode(currentValue: BigInteger): EncodeResult
    fun decode(str: String, index: Int): DecodeResult?
}