package by.cyberpartisan.psms.plain_data_encoder

import kotlin.test.Test
import kotlin.test.assertEquals

class Cp1251Test {
    private fun testEncodeDecode(str: String, expectedEncoded: ByteArray) {
        val encoder = Cp1251()
        val encoded = encoder.encode(str)
        assertEquals(expectedEncoded.toList(), encoded.toList(), "Invalid encoding.")
        val decoded = encoder.decode(encoded)
        assertEquals(str, decoded, "Invalid decoding.")
    }

    @Test
    fun testEmpty() {
        testEncodeDecode("", byteArrayOf())
    }

    @Test
    fun testSingleChar() {
        testEncodeDecode("ё", byteArrayOf((0xB8).toByte()))
    }

    @Test
    fun testMultipleChar() {
        testEncodeDecode("µ…x", byteArrayOf((0xB5).toByte(), (0x85).toByte(), (0x78).toByte()))
    }

    @Test
    fun testInvalidEncoding() {
        assertEquals('?'.toByte(), Cp1251().encode("π")[0], "Incorrect unknown char encoding.")
    }
}