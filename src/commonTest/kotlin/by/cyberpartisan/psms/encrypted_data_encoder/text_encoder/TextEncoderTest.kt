package by.cyberpartisan.psms.encrypted_data_encoder.text_encoder

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

class TextEncoderTest {
    @Test
    fun printTest() {
        for (i in 1..10) {
            val str = TextEncoder.instance.encode(Random.Default.nextBytes(48))
            println(str)
        }
    }

    private fun testEncodeDecode(data: ByteArray, expectedEncoded: String) {
        val encoder = TextEncoder.instance
        val encoded = encoder.encode(data)
        assertEquals(encoded, expectedEncoded, "Invalid encoded string.")
        val decoded = encoder.decode(encoded)
        assertEquals(data.toList(), decoded.toList(), "Invalid decoded data.")
    }

    @Test
    fun testEmpty() {
        testEncodeDecode(ByteArray(0), "")
    }
}