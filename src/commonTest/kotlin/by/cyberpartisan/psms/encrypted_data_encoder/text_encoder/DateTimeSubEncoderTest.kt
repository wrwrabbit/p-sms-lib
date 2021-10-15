package by.cyberpartisan.psms.encrypted_data_encoder.text_encoder

import com.ionspin.kotlin.bignum.integer.BigInteger
import kotlin.test.Test
import kotlin.test.assertEquals

class DateTimeSubEncoderTest : AbstractSubEncoderTest() {
    private val timeSize = 24 * 60
    private val dateSize = 365

    override fun getEncoder(): SubEncoder = DateTimeSubEncoder()

    @Test
    fun testFirstTime() {
        testEncodeDecode(0, EncodeResult(BigInteger(timeSize + dateSize), "00:00"))
    }

    @Test
    fun testLastTime() {
        testEncodeDecode(timeSize - 1, EncodeResult(BigInteger(timeSize + dateSize), "23:59"))
    }

    @Test
    fun testFirstDate() {
        testEncodeDecode(timeSize, EncodeResult(BigInteger(timeSize + dateSize), "01.01"))
    }

    @Test
    fun testLastDate() {
        testEncodeDecode(timeSize + dateSize - 1, EncodeResult(BigInteger(timeSize + dateSize), "31.12"))
    }

    @Test
    fun testOverflow() {
        testEncodeDecode(timeSize + dateSize, EncodeResult(BigInteger(timeSize + dateSize), "00:00"))
    }

    @Test
    fun testIntegerOverflow() {
        testEncodeDecode(BigInteger(4_294_967_295), EncodeResult(BigInteger(timeSize + dateSize), "08:00"))
    }

    @Test
    fun testMonthOverflow() {
        testEncodeDecode(timeSize + 31, EncodeResult(BigInteger(timeSize + dateSize), "01.02"))
    }

    @Test
    fun testAll() {
        val encoder = getEncoder()
        for (i in 0 until timeSize + dateSize) {
            assertEquals(i, encoder.decode(encoder.encode(BigInteger(i)).word, 0)?.value, "Encode decode error.")
        }
    }
}