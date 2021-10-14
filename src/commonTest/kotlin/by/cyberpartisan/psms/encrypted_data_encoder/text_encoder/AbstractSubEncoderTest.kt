package by.cyberpartisan.psms.encrypted_data_encoder.text_encoder

import com.ionspin.kotlin.bignum.integer.BigInteger
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

abstract class AbstractSubEncoderTest {
    protected fun testEncodeDecode(data: Int, expectedEncodeResult: EncodeResult) {
        val encoder = getEncoder()
        val encodeResult = encoder.encode(BigInteger(data))
        assertEquals(encodeResult, expectedEncodeResult, "Invalid encoded result.")
        val decodedResult = encoder.decode(encodeResult.word, 0)
        assertNotNull(decodedResult, "Decoding error.")
        assertEquals(decodedResult.size, encodeResult.size, "Invalid decoded size.")
        assertEquals(decodedResult.value, data % encodeResult.size.intValue(), "Invalid decoded data.")
        assertEquals(decodedResult.newPosition, encodeResult.word.length, "Invalid decode position.")
    }

    abstract fun getEncoder() : SubEncoder
}