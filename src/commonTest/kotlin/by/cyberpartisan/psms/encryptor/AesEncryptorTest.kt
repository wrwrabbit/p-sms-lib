package by.cyberpartisan.psms.encryptor

import kotlin.test.*

class AesEncryptorTest {
    private fun testEncryptDecrypt(data: ByteArray, key: ByteArray) {
        val encryptor = AesEncryptor()
        val encrypted = encryptor.encrypt(key, data)
        assertEquals(data.size + 4, encrypted.size, "Encrypted invalid size.")
        if (data.size > 4) {
            assertNotEquals(data.toList(), encrypted.slice(0 until encrypted.size - 4), "Data wasn't encrypted.")
        }
        val decrypted = encryptor.decrypt(key, encrypted)
        assertEquals(data.toList(), decrypted.toList(), "Decrypted data was incorrect.")
    }

    @Test
    fun testEncryptDecryptEmpty() {
        testEncryptDecrypt(ByteArray(0), ByteArray(256 / 8))
    }

    @Test
    fun testEncryptDecryptShort256() {
        testEncryptDecrypt(ByteArray(1) { it.toByte() }, ByteArray(256 / 8))
    }

    @Test
    fun testEncryptDecryptShort192() {
        testEncryptDecrypt(ByteArray(1) { it.toByte() }, ByteArray(192 / 8))
    }

    @Test
    fun testEncryptDecryptShort128() {
        testEncryptDecrypt(ByteArray(1) { it.toByte() }, ByteArray(128 / 8))
    }

    @Test
    fun testEncryptDecryptBlock() {
        testEncryptDecrypt(ByteArray(128 / 8) { it.toByte() }, ByteArray(256 / 8))
    }

    @Test
    fun testEncryptDecryptBlockAndOne() {
        testEncryptDecrypt(ByteArray(128 / 8 + 1) { it.toByte() }, ByteArray(256 / 8))
    }

    @Test
    fun testEncryptDecryptManyBlocks() {
        testEncryptDecrypt(ByteArray(128 * 3 / 8) { it.toByte() }, ByteArray(256 / 8))
    }

    @Test
    fun testEmptyKey() {
        assertFailsWith<InvalidKeyException> { AesEncryptor().encrypt(ByteArray(0), ByteArray(0)) }
        assertFailsWith<InvalidKeyException> { AesEncryptor().decrypt(ByteArray(0), ByteArray(0)) }
    }

    @Test
    fun testSmallKey() {
        assertFailsWith<InvalidKeyException> { AesEncryptor().encrypt(ByteArray(0), ByteArray(5)) }
        assertFailsWith<InvalidKeyException> { AesEncryptor().decrypt(ByteArray(0), ByteArray(0)) }
    }

    @Test
    fun testBigKey() {
        assertFailsWith<InvalidKeyException> { AesEncryptor().encrypt(ByteArray(0), ByteArray(256 / 8 + 1)) }
        assertFailsWith<InvalidKeyException> { AesEncryptor().decrypt(ByteArray(0), ByteArray(0)) }
    }
}