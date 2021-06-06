package by.cyberpartisan.psms.encrypted_data_encoder

import by.cyberpartisan.psms.encrypted_data_encoder.text_encoder.TextEncoder
import kotlin.random.Random
import kotlin.test.Test

class TextEncoderTest {
    @Test
    fun printTest() {
        for (i in 1..10) {
            val str = TextEncoder().encode(Random.Default.nextBytes(48))
            println(str)
        }
    }
}