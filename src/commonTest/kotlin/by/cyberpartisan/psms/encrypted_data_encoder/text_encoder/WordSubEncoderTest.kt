package by.cyberpartisan.psms.encrypted_data_encoder.text_encoder

import com.ionspin.kotlin.bignum.integer.BigInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class WordSubEncoderTest : AbstractSubEncoderTest() {
    private val size = 10

    override fun getEncoder(): SubEncoder = WordsSubEncoder(listOf("10", "11", "12", "13", "14", "15", "16", "17", "100", "19"))

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

    @Test
    fun testIntegerOverflow() {
        testEncodeDecode(BigInteger(4_294_967_295), EncodeResult(BigInteger(size), "15"))
    }

    @Test
    fun testInvalidPrefix() {
        val encoder = getEncoder()
        val decodedResult = encoder.decode("101", 0)
        assertNull(decodedResult, "Prefix treated as word.")
    }

    @Test
    fun testValidPrefix() {
        val encoder = getEncoder()
        val decodedResult = encoder.decode("100", 0)
        assertNotNull(decodedResult, "Prefix treated as word.")
    }

    @Test
    fun testDate() {
        val encoder = getEncoder()
        val decodedResult = encoder.decode("10.10", 0)
        assertNull(decodedResult, "Date treated as number.")
    }
}