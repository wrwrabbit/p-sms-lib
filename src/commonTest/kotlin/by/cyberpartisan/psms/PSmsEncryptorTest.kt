package by.cyberpartisan.psms

import by.cyberpartisan.psms.encrypted_data_encoder.EncryptedDataEncoder
import by.cyberpartisan.psms.encrypted_data_encoder.EncryptedDataEncoderFactory
import by.cyberpartisan.psms.encryptor.Encryptor
import by.cyberpartisan.psms.plain_data_encoder.PlainDataEncoder
import by.cyberpartisan.psms.plain_data_encoder.PlainDataEncoderFactory
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue

@ExperimentalUnsignedTypes
class PSmsEncryptorTest {

    class PlainDataEncoderMock : PlainDataEncoder {
        override fun encode(s: String): ByteArray = s.encodeToByteArray()
        override fun decode(data: ByteArray): String = data.decodeToString()
        override fun getMode(): Int = 42
    }

    class PlainDataEncoderFactoryMock : PlainDataEncoderFactory {
        val encoder = PlainDataEncoderMock()
        override fun create(mode: Int): PlainDataEncoder = encoder
        override fun createBestEncoder(s: String): PlainDataEncoder = encoder
    }

    private val plainFactory = PlainDataEncoderFactoryMock()

    class EncryptedDataEncoderMock : EncryptedDataEncoder {
        override fun hasFrontPadding(): Boolean = false

        override fun encode(data: ByteArray): String {
            return data.asUByteArray().joinToString("") { it.toString(16).padStart(2, '0') }
        }
        override fun decode(str: String): ByteArray {
            assertEquals(0, str.length % 2, "String has odd length.")
            return str.chunked(2)
                .map { it.toInt(16).toByte() }
                .toByteArray()
        }
    }

    class EncryptedDataEncoderFactoryMock : EncryptedDataEncoderFactory {
        private val encoder = EncryptedDataEncoderMock()
        override fun create(schemeId: Int) : EncryptedDataEncoder = encoder
    }

    private val encryptedFactory = EncryptedDataEncoderFactoryMock()

    class EncryptorMock : Encryptor {
        var usedKeyEncrypt: ByteArray? = null
        var usedKeyDecrypt: ByteArray? = null
        override fun encrypt(key: ByteArray, plainData: ByteArray): ByteArray {
            usedKeyEncrypt = key
            return plainData
        }
        override fun decrypt(key: ByteArray, encryptedData: ByteArray): ByteArray {
            usedKeyDecrypt = key
            return encryptedData
        }
    }

    private val encryptor = EncryptorMock()

    private fun createPSmsEncryptor(): PSmsEncryptor = PSmsEncryptor(plainFactory, encryptedFactory, encryptor)

    private fun checkEncryptedData(plainString: String, data: ByteArray) {
        val encoder = PlainDataEncoderMock()
        val encodedStringSize = encoder.encode(plainString).size
        assertEquals(encodedStringSize + 1 + HASH_SIZE, data.size, "Invalid data size.")
        val decodedString = encoder.decode(data.slice(0 until encodedStringSize).toByteArray())
        assertEquals(plainString, decodedString, "Strings are not equal.")
        val calculatedMd5 = md5(data.slice(plainString.encodeToByteArray().indices).toByteArray()).slice(0 until HASH_SIZE)
        val md5FromData = data.slice(data.size - HASH_SIZE until data.size)
        assertEquals(calculatedMd5, md5FromData, "Hash invalid.")
        assertEquals(plainFactory.encoder.getMode().toByte(), data[data.size - HASH_SIZE - 1], "Invalid mode.")
    }

    private fun testEncodeDecode(str: String) {
        val pSmsEncryptor = createPSmsEncryptor()
        val encoded = pSmsEncryptor.encode(str, ByteArray(0), 0)
        assertEquals(listOf(), encryptor.usedKeyEncrypt?.toList(), "Invalid used key.")
        val encryptedEncoder = EncryptedDataEncoderMock()
        checkEncryptedData(str, encryptedEncoder.decode(encoded))
        assertTrue(pSmsEncryptor.isEncrypted(encoded, ByteArray(0)), "String must be encrypted.")
        val decoded = pSmsEncryptor.decode(encoded, ByteArray(0), 0)
        assertEquals(listOf(), encryptor.usedKeyDecrypt?.toList(), "Invalid used key.")
        assertEquals(str, decoded, "Encoded and decoded strings are different.")
        val tryDecoded = pSmsEncryptor.tryDecode(encoded, ByteArray(0))
        assertEquals(str, tryDecoded, "Encoded and decoded by 'tryDecode' strings are different.")
    }

    @Test
    fun testEncodeDecodeEmpty() {
        testEncodeDecode("")
    }

    @Test
    fun testEncodeDecodeSingleChar() {
        testEncodeDecode("a")
    }

    @Test
    fun testEncodeDecodeLong() {
        testEncodeDecode("1234567890123456789012345678901234567890")
    }

    @Test
    fun testEncodeDecodeUnicode() {
        testEncodeDecode("\uD83D\uDE02")
    }

    private fun testModifiedEncrypted(modifyEncoded: (str: String) -> String) {
        val pSmsEncryptor = createPSmsEncryptor()
        val encoded = pSmsEncryptor.encode("1234567890123456789012345678901234567890", ByteArray(0), 0)
        val encodedModified = modifyEncoded(encoded)
        assertEquals(false, pSmsEncryptor.isEncrypted(encodedModified, ByteArray(0)), "Modification ignored by isEncrypted.")
        assertEquals(encodedModified, pSmsEncryptor.tryDecode(encodedModified, ByteArray(0)), "Modification ignored by tryDecode.")
        assertFails { pSmsEncryptor.decode(encodedModified, ByteArray(0), 0) }
    }

    @Test
    fun testNotEncryptedModifiedHash() {
        testModifiedEncrypted { encoded -> encoded.substring(0, encoded.length - 1) + (if (encoded.last() == '0') '1' else '0') }
    }

    @Test
    fun testNotEncryptedModifiedBody() {
        testModifiedEncrypted { encoded -> (if (encoded.first() == '0') '1' else '0') + encoded.substring(1, encoded.length) }
    }

    private fun testNotEncrypted(str: String) {
        val pSmsEncryptor = createPSmsEncryptor()
        assertEquals(false, pSmsEncryptor.isEncrypted(str, ByteArray(0)), "Not encrypted string ignored by isEncrypted.")
        assertEquals(str, pSmsEncryptor.tryDecode(str, ByteArray(0)), "Not encrypted string ignored by tryDecode.")
        assertFails { pSmsEncryptor.decode(str, ByteArray(0), 0) }
    }

    @Test
    fun testNotEncryptedEmpty() {
        testNotEncrypted("")
    }

    @Test
    fun testNotEncryptedRaw() {
        testNotEncrypted("raw")
    }

    @Test
    fun testNotEncryptedShortHex() {
        testNotEncrypted("00")
    }

    @Test
    fun testNotEncryptedLongHex() {
        testNotEncrypted("00000000")
    }

    class PaddingEncryptedDataEncoderMock : EncryptedDataEncoder {
        val paddingSize = 2
        override fun hasFrontPadding(): Boolean = true

        override fun encode(data: ByteArray): String {
            return "FF".repeat(paddingSize) + data.asUByteArray().joinToString("") { it.toString(16).padStart(2, '0') }
        }
        override fun decode(str: String): ByteArray {
            assertEquals(0, str.length % 2, "String has odd length.")
            return str.chunked(2)
                .map { it.toInt(16).toByte() }
                .toByteArray()
        }
    }

    class PaddedEncryptedDataEncoderFactoryMock : EncryptedDataEncoderFactory {
        val encoder = PaddingEncryptedDataEncoderMock()
        override fun create(schemeId: Int) : EncryptedDataEncoder = encoder
    }

    private val paddedEncryptedFactory = PaddedEncryptedDataEncoderFactoryMock()

    private fun checkPaddedEncryptedData(plainString: String, data: ByteArray) {
        val encoder = PlainDataEncoderMock()
        val encodedStringSize = encoder.encode(plainString).size
        assertEquals(encodedStringSize + 1 + HASH_SIZE + 1, data.size, "Invalid data size.")
        val decodedString = encoder.decode(data.slice(0 until encodedStringSize).toByteArray())
        assertEquals(plainString, decodedString, "Strings are not equal.")
        val calculatedMd5 = md5(data.slice(plainString.encodeToByteArray().indices).toByteArray()).slice(0 until HASH_SIZE)
        val md5FromData = data.slice(data.size - HASH_SIZE - 1 until data.size - 1)
        assertEquals(calculatedMd5, md5FromData, "Hash invalid.")
        assertEquals(plainFactory.encoder.getMode().toByte(), data[data.size - HASH_SIZE - 1 - 1], "Invalid mode.")
    }

    private fun testPaddedEncodeDecode(str: String) {
        val pSmsEncryptor = PSmsEncryptor(plainFactory, paddedEncryptedFactory, encryptor)
        val encoded = pSmsEncryptor.encode(str, ByteArray(0), 0)
        val paddingSize = paddedEncryptedFactory.encoder.paddingSize
        assertEquals(listOf(), encryptor.usedKeyEncrypt?.toList(), "Invalid used key.")
        val encryptedEncoder = EncryptedDataEncoderMock()
        checkPaddedEncryptedData(str, encryptedEncoder.decode(encoded.slice(paddingSize * 2 until encoded.length)))
        assertTrue(pSmsEncryptor.isEncrypted(encoded, ByteArray(0)), "String must be encrypted.")
        val decoded = pSmsEncryptor.decode(encoded, ByteArray(0), 0)
        assertEquals(listOf(), encryptor.usedKeyDecrypt?.toList(), "Invalid used key.")
        assertEquals(str, decoded, "Encoded and decoded strings are different.")
        val tryDecoded = pSmsEncryptor.tryDecode(encoded, ByteArray(0))
        assertEquals(str, tryDecoded, "Encoded and decoded by 'tryDecode' strings are different.")
    }

    private fun testEncodeDecodePadded(str: String) {
        testPaddedEncodeDecode(str)
    }

    @Test
    fun testPaddedEncodeDecodeEmpty() {
        testEncodeDecodePadded("")
    }

    @Test
    fun testPaddedEncodeDecodeSingleChar() {
        testEncodeDecodePadded("a")
    }

    @Test
    fun testPaddedEncodeDecodeLong() {
        testEncodeDecodePadded("1234567890123456789012345678901234567890")
    }

    @Test
    fun testPaddedEncodeDecodeUnicode() {
        testEncodeDecodePadded("\uD83D\uDE02")
    }
}