package by.cyberpartisan.psms.encrypted_data_encoder.text_encoder

import com.ionspin.kotlin.bignum.integer.BigInteger
import kotlin.test.Test

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
}