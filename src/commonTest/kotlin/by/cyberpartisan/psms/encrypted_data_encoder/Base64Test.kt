package by.cyberpartisan.psms.encrypted_data_encoder

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class Base64Test {
    private fun testEncodeDecode(data: ByteArray, expectedEncoded: String) {
        val encoder = Base64()
        val encoded = encoder.encode(data)
        assertEquals(encoded, expectedEncoded, "Invalid encoded string.")
        val decoded = encoder.decode(encoded)
        assertEquals(data.toList(), decoded.toList(), "Invalid decoded data.")
    }

    @Test
    fun testEmpty() {
        testEncodeDecode(ByteArray(0), "")
    }

    @Test
    fun testSingle() {
        testEncodeDecode(byteArrayOf(0), "AA==")
    }

    @Test
    fun testLong() {
        testEncodeDecode(byteArrayOf(1, 2, 3), "AQID")
    }

    @Test
    fun testInvalidChars() {
        assertFails { Base64().decode("!") }
    }
}