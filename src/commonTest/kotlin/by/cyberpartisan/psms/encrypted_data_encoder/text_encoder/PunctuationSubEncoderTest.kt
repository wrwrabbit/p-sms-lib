package by.cyberpartisan.psms.encrypted_data_encoder.text_encoder

import com.ionspin.kotlin.bignum.integer.BigInteger
import kotlin.test.Test

class PunctuationSubEncoderTest : AbstractSubEncoderTest() {
    private val size = 7

    override fun getEncoder(): SubEncoder = PunctuationSubEncoder()

    @Test
    fun testFirst() {
        testEncodeDecode(0, EncodeResult(BigInteger(size), ",", needSpaceBefore = false))
    }

    @Test
    fun testLast() {
        testEncodeDecode(size - 1, EncodeResult(BigInteger(size), "!", needSpaceBefore = false))
    }

    @Test
    fun testOverflow() {
        testEncodeDecode(size, EncodeResult(BigInteger(size), ",", needSpaceBefore = false))
    }
}
