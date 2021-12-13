package by.cyberpartisan.psms.encrypted_data_encoder.text_encoder

import com.ionspin.kotlin.bignum.integer.BigInteger

data class EncodeResult(
    val size: BigInteger,
    val word: String,
    val needSpaceBefore: Boolean = true,
    val needSpaceAfter: Boolean = true
)
