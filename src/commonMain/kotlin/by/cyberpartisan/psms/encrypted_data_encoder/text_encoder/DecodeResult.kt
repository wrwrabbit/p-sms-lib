package by.cyberpartisan.psms.encrypted_data_encoder.text_encoder

import com.ionspin.kotlin.bignum.integer.BigInteger

data class DecodeResult(
    val size: Int,
    val value: Int,
    val newPosition: Int,
    val needSpaceBefore: Boolean = true,
    val needSpaceAfter: Boolean = true
)
