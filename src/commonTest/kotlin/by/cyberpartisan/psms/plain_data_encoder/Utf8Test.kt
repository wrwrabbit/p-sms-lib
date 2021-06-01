package by.cyberpartisan.psms.plain_data_encoder

import kotlin.test.Test
import kotlin.test.assertEquals

class Utf8Test {
    private fun testEncodeDecode(str: String, expectedEncoded: ByteArray) {
        val encoder = Utf8()
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
        testEncodeDecode("ё", byteArrayOf((0xd1).toByte(), (0x91).toByte()))
    }

    @Test
    fun testMultipleChar() {
        testEncodeDecode("πў", byteArrayOf((0xCF).toByte(), (0x80).toByte(), (0xD1).toByte(), (0x9E).toByte()))
    }
}