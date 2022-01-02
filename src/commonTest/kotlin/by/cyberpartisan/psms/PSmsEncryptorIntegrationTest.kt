package by.cyberpartisan.psms

import by.cyberpartisan.psms.encrypted_data_encoder.Scheme
import com.soywiz.krypto.encoding.Base64
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PSmsEncryptorIntegrationTest {
    private fun testEncodeDecode(str: String, key: ByteArray) {
        testEncodeDecode(Message(str), key)
    }

    private fun testEncodeDecode(message: Message, key: ByteArray, schemeId: Int = 0) {
        val pSmsEncryptor = PSmsEncryptor()
        val encoded = pSmsEncryptor.encode(message, key, schemeId)
        assertTrue(pSmsEncryptor.isEncrypted(encoded, key), "String must be encrypted.")
        val decoded = pSmsEncryptor.decode(encoded, key, schemeId)
        assertEquals(message, decoded, "Encoded and decoded messages are different.")
        val tryDecoded = pSmsEncryptor.tryDecode(encoded, key)
        assertEquals(message, tryDecoded, "Encoded and decoded by 'tryDecode' messages are different.")
    }

    @Test
    fun testEncodeDecodeEmpty() {
        testEncodeDecode("", Random.nextBytes(16))
    }

    @Test
    fun testEncodeDecodeSingleChar() {
        testEncodeDecode("a", Random.nextBytes(24))
    }

    @Test
    fun testEncodeDecodeLong() {
        testEncodeDecode("1234567890123456789012345678901234567890", Random.nextBytes(24))
    }

    @Test
    fun testEncodeDecodeChannel() {
        testEncodeDecode(Message("1234567890123456789012345678901234567890", 124), Random.nextBytes(16))
    }

    @Test
    fun testEncodeDecodeText() {
        val wordsScheme = Scheme.TEXT
        testEncodeDecode(Message("WORD"), Random.nextBytes(32), wordsScheme.ordinal)
    }

    @Test
    fun encrypt() {
        val pSmsEncryptor = PSmsEncryptor()
        val encoded = pSmsEncryptor.encode(Message("Super secret channel", 5), Base64.decode("w4OTV4i3zbE6z/U7Sh2WoZpvl4TT1pCltprSca56AIs="), 0)
        println(encoded)
    }

    @Test
    fun decrypt() {
        val pSmsEncryptor = PSmsEncryptor()
        val decoded = pSmsEncryptor.tryDecode("окисел дичая путча Стервою 2 зачалил Аслан", Base64.decode("w4OTV4i3zbE6z/U7Sh2WoZpvl4TT1pCltprSca56AIs="))
        println(decoded.text)
    }
}