package by.cyberpartisan.psms

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PSmsEncryptorIntegrationTest {
    private fun testEncodeDecode(str: String, keySrc: String) {
        val pSmsEncryptor = PSmsEncryptor()
        val key = keySrc.encodeToByteArray()//md5(keySrc.encodeToByteArray())
        val encoded = pSmsEncryptor.encode(str, key, 0)
        assertTrue(pSmsEncryptor.isEncrypted(encoded, key), "String must be encrypted.")
        val decoded = pSmsEncryptor.decode(encoded, key, 0)
        assertEquals(str, decoded, "Encoded and decoded strings are different.")
        val tryDecoded = pSmsEncryptor.tryDecode(encoded, key)
        assertEquals(str, tryDecoded, "Encoded and decoded by 'tryDecode' strings are different.")
    }

    @Test
    fun testEncodeDecodeEmpty() {
        testEncodeDecode("", "key")
    }

    @Test
    fun testEncodeDecodeSingleChar() {
        testEncodeDecode("a", "key")
    }

    @Test
    fun testEncodeDecodeLong() {
        testEncodeDecode("1234567890123456789012345678901234567890", "key")
    }
}