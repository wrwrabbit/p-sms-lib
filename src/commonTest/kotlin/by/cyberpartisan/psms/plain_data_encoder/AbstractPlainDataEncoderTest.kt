package by.cyberpartisan.psms.plain_data_encoder

import kotlin.test.assertEquals

abstract class AbstractPlainDataEncoderTest {
    abstract fun encoder() : PlainDataEncoder

    protected fun testEncodeDecode(str: String, expectedEncoded: ByteArray? = null, expectedDecoded: String? = null) {
        val encoder = encoder()
        val encoded = encoder.encode(str)
        if (expectedEncoded != null) {
            assertEquals(expectedEncoded.toList(), encoded.toList(), "Invalid encoding.")
        }
        val decoded = encoder.decode(encoded)
        assertEquals(expectedDecoded ?: str, decoded, "Invalid decoding.")
    }

    protected fun bytes(vararg values: Int): ByteArray {
        return values.map { it.toByte() }.toByteArray()
    }
}