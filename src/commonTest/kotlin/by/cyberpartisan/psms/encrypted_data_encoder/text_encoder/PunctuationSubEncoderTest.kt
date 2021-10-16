package by.cyberpartisan.psms.encrypted_data_encoder.text_encoder

import com.ionspin.kotlin.bignum.integer.BigInteger
import kotlin.test.Test
import kotlin.test.assertEquals

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

    @Test
    fun testIntegerOverflow() {
        testEncodeDecode(BigInteger(4_294_967_295), EncodeResult(BigInteger(size), ")", needSpaceBefore = false))
    }

    @Test
    fun testSpace() {
        val decodedResult = getEncoder().decode(", ", 0)
        assertEquals(2, decodedResult?.newPosition, "Space was not taken")
        testEncodeDecode(size, EncodeResult(BigInteger(size), ",", needSpaceBefore = false))
    }
}
