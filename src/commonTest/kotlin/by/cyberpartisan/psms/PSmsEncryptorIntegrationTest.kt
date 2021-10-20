package by.cyberpartisan.psms

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PSmsEncryptorIntegrationTest {
    private fun testEncodeDecode(str: String, key: ByteArray) {
        testEncodeDecode(Message(str), key)
    }

    private fun testEncodeDecode(message: Message, key: ByteArray) {
        val pSmsEncryptor = PSmsEncryptor()
        val encoded = pSmsEncryptor.encode(message, key, 0)
        assertTrue(pSmsEncryptor.isEncrypted(encoded, key), "String must be encrypted.")
        val decoded = pSmsEncryptor.decode(encoded, key, 0)
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
}