package by.cyberpartisan.psms.encrypted_data_encoder.text_encoder

import com.ionspin.kotlin.bignum.integer.BigInteger
import kotlin.test.Test

class WordSubEncoderTest : AbstractSubEncoderTest() {
    private val size = 10

    override fun getEncoder(): SubEncoder = WordsSubEncoder(listOf("10", "11", "12", "13", "14", "15", "16", "17", "18", "19"))

    @Test
    fun testFirst() {
        testEncodeDecode(0, EncodeResult(BigInteger(size), "10"))
    }

    @Test
    fun testLast() {
        testEncodeDecode(size - 1, EncodeResult(BigInteger(size), "19"))
    }

    @Test
    fun testOverflow() {
        testEncodeDecode(size, EncodeResult(BigInteger(size), "10"))
    }
}