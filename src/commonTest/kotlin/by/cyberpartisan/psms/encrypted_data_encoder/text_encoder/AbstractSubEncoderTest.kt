package by.cyberpartisan.psms.encrypted_data_encoder.text_encoder

import com.ionspin.kotlin.bignum.integer.BigInteger
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

abstract class AbstractSubEncoderTest {
    protected fun testEncodeDecode(data: Int, expectedEncodeResult: EncodeResult) {
        testEncodeDecode(BigInteger(data), expectedEncodeResult)
    }

    protected fun testEncodeDecode(data: BigInteger, expectedEncodeResult: EncodeResult) {
        val encoder = getEncoder()
        val encodeResult = encoder.encode(data)
        assertEquals(expectedEncodeResult, encodeResult, "Invalid encoded result.")
        val decodedResult = encoder.decode(encodeResult.word, 0)
        assertNotNull(decodedResult, "Decoding error.")
        assertEquals(encodeResult.size.intValue(), decodedResult.size, "Invalid decoded size.")
        assertEquals((data % encodeResult.size).intValue(), decodedResult.value, "Invalid decoded data.")
        assertEquals(encodeResult.word.length, decodedResult.newPosition, "Invalid decode position.")
    }

    abstract fun getEncoder() : SubEncoder
}