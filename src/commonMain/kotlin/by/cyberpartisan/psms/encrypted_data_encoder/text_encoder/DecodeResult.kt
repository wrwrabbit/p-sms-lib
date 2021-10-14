package by.cyberpartisan.psms.encrypted_data_encoder.text_encoder

import com.ionspin.kotlin.bignum.integer.BigInteger

data class DecodeResult(
    val size: BigInteger,
    val value: Int,
    val newPosition: Int
)
