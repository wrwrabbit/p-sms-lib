package by.cyberpartisan.psms

import by.cyberpartisan.psms.encrypted_data_encoder.EncryptedDataEncoder
import by.cyberpartisan.psms.encrypted_data_encoder.EncryptedDataEncoderFactory
import by.cyberpartisan.psms.encryptor.Encryptor
import by.cyberpartisan.psms.plain_data_encoder.Mode
import by.cyberpartisan.psms.plain_data_encoder.PlainDataEncoder
import by.cyberpartisan.psms.plain_data_encoder.PlainDataEncoderFactory
import com.soywiz.krypto.HMAC
import kotlin.test.*

@ExperimentalUnsignedTypes
class PSmsEncryptorTest {

    class PlainDataEncoderMock : PlainDataEncoder {
        override fun encode(s: String): ByteArray = s.encodeToByteArray()
        override fun decode(data: ByteArray): String = data.decodeToString()
        override fun getMode(): Int = 42 and 0x0F
    }

    class PlainDataEncoderFactoryMock: PlainDataEncoderFactory {
        val encoder = PlainDataEncoderMock()
        override fun create(mode: Int): PlainDataEncoder = encoder
        override fun create(mode: Mode): PlainDataEncoder = encoder
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
            try {
                return str.chunked(2)
                    .map { it.toInt(16).toByte() }
                    .toByteArray()
            } catch (ignored: NumberFormatException) {
                throw InvalidDataException()
            }
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

    private fun checkEncryptedData(message: Message, data: ByteArray, hash: (data: ByteArray) -> ByteArray) {
        val encoder = PlainDataEncoderMock()
        val encodedStringSize = encoder.encode(message.text).size
        val channelIdSize = if (message.channelId != null) CHANNEL_ID_SIZE else 0
        assertEquals(encodedStringSize + 1 + HASH_SIZE + channelIdSize, data.size, "Invalid data size.")
        val decodedString = encoder.decode(data.slice(0 until encodedStringSize).toByteArray())
        assertEquals(message.text, decodedString, "Strings are not equal.")
        val payloadByteCount = message.text.encodeToByteArray().size + channelIdSize
        val calculatedHash = hash(data.slice(0 until payloadByteCount).toByteArray()).slice(0 until HASH_SIZE)
        val hashFromData = data.slice(data.size - HASH_SIZE until data.size)
        assertEquals(calculatedHash, hashFromData, "Hash invalid.")
        assertEquals(plainFactory.encoder.getMode(), data[data.size - HASH_SIZE - 1].toInt() and 0x0F, "Invalid mode.")
        assertEquals(VERSION, (data[data.size - HASH_SIZE - 1].toInt() and 0x70) shr 4, "Invalid version.")
        assertEquals(message.channelId != null, (data[data.size - HASH_SIZE - 1].toInt() and 0x80) != 0, "Invalid isChannel.")
        if (message.channelId != null) {
            val channelId = (((((data[data.size - HASH_SIZE - 1 - 1].toInt() shl 8) or
                    data[data.size - HASH_SIZE - 1 - 2].toInt()) shl 8) or
                    data[data.size - HASH_SIZE - 1 - 3].toInt()) shl 8) or
                    data[data.size - HASH_SIZE - 1 - 4].toInt()
            assertEquals(message.channelId, channelId, "Invalid channelId.")
        }
    }

    private fun testEncodeDecode(str: String, legacy: Boolean) {
        testEncodeDecode(Message(str, isLegacy = legacy), legacy)
    }

    private fun testEncodeDecode(message: Message, legacy: Boolean) {
        val key = ByteArray(0)
        val pSmsEncryptor = createPSmsEncryptor()
        val encoded = if (legacy) pSmsEncryptor.encodeLegacy(message, key, 0)
            else pSmsEncryptor.encode(message, key, 0)
        assertEquals(listOf(), encryptor.usedKeyEncrypt?.toList(), "Invalid used key.")
        val encryptedEncoder = EncryptedDataEncoderMock()
        checkEncryptedData(message, encryptedEncoder.decode(encoded)) { d ->
            if (legacy) {
                md5(d)
            } else {
                HMAC.hmacSHA256(key, d).bytes
            }
        }
        assertTrue(pSmsEncryptor.isEncrypted(encoded, key), "String must be encrypted.")
        val decoded = if (legacy) pSmsEncryptor.decodeLegacy(encoded, key, 0)
            else pSmsEncryptor.decode(encoded, key, 0)
        assertEquals(listOf(), encryptor.usedKeyDecrypt?.toList(), "Invalid used key.")
        assertEquals(message.text, decoded.text, "Encoded and decoded strings are different.")
        assertEquals(message.channelId, decoded.channelId, "Encoded and decoded channel ids are different.")
        assertEquals(message.isLegacy, decoded.isLegacy, "Legacies are different.")
        val tryDecoded = pSmsEncryptor.tryDecode(encoded, key)
        assertEquals(message.text, tryDecoded.text, "Encoded and decoded by 'tryDecode' strings are different.")
        assertEquals(message.channelId, tryDecoded.channelId, "Encoded and decoded by 'tryDecode' channel ids are different.")
    }

    @Test
    fun testEncodeDecodeEmptyLegacy() {
        testEncodeDecode("", true)
    }

    @Test
    fun testEncodeDecodeEmpty() {
        testEncodeDecode("", false)
    }

    @Test
    fun testEncodeDecodeSingleCharLegacy() {
        testEncodeDecode("a", true)
    }

    @Test
    fun testEncodeDecodeSingleChar() {
        testEncodeDecode("a", false)
    }

    @Test
    fun testEncodeDecodeLongLegacy() {
        testEncodeDecode("1234567890123456789012345678901234567890", true)
    }

    @Test
    fun testEncodeDecodeLong() {
        testEncodeDecode("1234567890123456789012345678901234567890", false)
    }

    @Test
    fun testEncodeDecodeUnicodeLegacy() {
        testEncodeDecode("\uD83D\uDE02", true)
    }

    @Test
    fun testEncodeDecodeUnicode() {
        testEncodeDecode("\uD83D\uDE02", false)
    }

    @Test
    fun testEncodeDecodeChannelIdLegacy() {
        testEncodeDecode(Message("1234567890123456789012345678901234567890", 42, true), true)
    }

    @Test
    fun testEncodeDecodeChannelId() {
        testEncodeDecode(Message("1234567890123456789012345678901234567890", 42), false)
    }

    private fun testModifiedEncrypted(modifyEncoded: (str: String) -> String) {
        val key = ByteArray(0)
        val pSmsEncryptor = createPSmsEncryptor()
        val encoded = pSmsEncryptor.encode(Message("1234567890123456789012345678901234567890"), key, 0)
        val encodedModified = modifyEncoded(encoded)
        assertEquals(false, pSmsEncryptor.isEncrypted(encodedModified, key), "Modification ignored by isEncrypted.")
        assertEquals(encodedModified, pSmsEncryptor.tryDecode(encodedModified, key).text, "Modification ignored by tryDecode.")
        assertFailsWith<InvalidDataException> { pSmsEncryptor.decode(encodedModified, key, 0) }
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
        val key = ByteArray(0)
        val pSmsEncryptor = createPSmsEncryptor()
        assertEquals(false, pSmsEncryptor.isEncrypted(str, key), "Not encrypted string ignored by isEncrypted.")
        assertEquals(str, pSmsEncryptor.tryDecode(str, key).text, "Not encrypted string ignored by tryDecode.")
        assertFails { pSmsEncryptor.decode(str, key, 0) }
    }

    @Test
    fun testNotEncryptedEmpty() {
        testNotEncrypted("")
    }

    @Test
    fun testNotEncryptedRaw() {
        testNotEncrypted("raw1")
    }

    @Test
    fun testNotEncryptedShortHex() {
        testNotEncrypted("00")
    }

    @Test
    fun testNotEncryptedLongHex() {
        testNotEncrypted("00000000")
    }

    class EncryptorModifyVersionId : Encryptor {
        override fun encrypt(key: ByteArray, plainData: ByteArray): ByteArray {
            val metaInfo = MetaInfo.parse(plainData[plainData.size - HASH_SIZE - 1])
            val changedMetaInfo = MetaInfo(metaInfo.mode, metaInfo.version + 1, metaInfo.isChannel)
            return plainData.slice(0 until plainData.size - HASH_SIZE - 1).toByteArray() +
                    byteArrayOf(changedMetaInfo.toByte()) +
                    plainData.slice(plainData.size - HASH_SIZE until plainData.size)
        }
        override fun decrypt(key: ByteArray, encryptedData: ByteArray): ByteArray {
            return encryptedData
        }
    }

    @Test
    fun testInvalidVersionId() {
        val key = ByteArray(0)
        val pSmsEncryptor = PSmsEncryptor(plainFactory, encryptedFactory, EncryptorModifyVersionId())
        val encoded = pSmsEncryptor.encode(Message("1234567890123456789012345678901234567890"), key, 0)
        assertFailsWith<InvalidVersionException> { pSmsEncryptor.decode(encoded, key, 0) }
        assertFailsWith<InvalidVersionException> { pSmsEncryptor.isEncrypted(encoded, key) }
        assertFailsWith<InvalidVersionException> { pSmsEncryptor.tryDecode(encoded, key) }
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

    private fun checkPaddedEncryptedData(plainString: String, data: ByteArray, hash: (data: ByteArray) -> ByteArray) {
        val encoder = PlainDataEncoderMock()
        val encodedStringSize = encoder.encode(plainString).size
        assertEquals(encodedStringSize + 1 + HASH_SIZE, data.size, "Invalid data size.")
        val decodedString = encoder.decode(data.slice(0 until encodedStringSize).toByteArray())
        assertEquals(plainString, decodedString, "Strings are not equal.")
        val calculatedHash = hash(data.slice(plainString.encodeToByteArray().indices).toByteArray()).slice(0 until HASH_SIZE)
        val hashFromData = data.slice(data.size - HASH_SIZE until data.size)
        assertEquals(calculatedHash, hashFromData, "Hash invalid.")
        assertEquals(plainFactory.encoder.getMode().toByte(), data[data.size - HASH_SIZE - 1], "Invalid mode.")
    }

    private fun testPaddedEncodeDecode(str: String, legacy: Boolean) {
        val key = ByteArray(0)
        val pSmsEncryptor = PSmsEncryptor(plainFactory, paddedEncryptedFactory, encryptor)
        val encoded = if (legacy) pSmsEncryptor.encodeLegacy(Message(str), key, 0)
            else pSmsEncryptor.encode(Message(str), key, 0)
        val paddingSize = paddedEncryptedFactory.encoder.paddingSize
        assertEquals(listOf(), encryptor.usedKeyEncrypt?.toList(), "Invalid used key.")
        val encryptedEncoder = EncryptedDataEncoderMock()
        checkPaddedEncryptedData(str, encryptedEncoder.decode(encoded.slice(paddingSize * 2 until encoded.length))) { d ->
            if (legacy) {
                md5(d)
            } else {
                HMAC.hmacSHA256(key, d).bytes
            }
        }
        assertTrue(pSmsEncryptor.isEncrypted(encoded, key), "String must be encrypted.")
        val decoded = if (legacy) pSmsEncryptor.decodeLegacy(encoded, key, 0)
            else pSmsEncryptor.decode(encoded, key, 0)
        assertEquals(listOf(), encryptor.usedKeyDecrypt?.toList(), "Invalid used key.")
        assertEquals(str, decoded.text, "Encoded and decoded strings are different.")
        assertNull(decoded.channelId, "ChannelId not null.")
        assertEquals(legacy, decoded.isLegacy, "Legacies are different.")
        val tryDecoded = pSmsEncryptor.tryDecode(encoded, key)
        assertEquals(str, tryDecoded.text, "Encoded and decoded by 'tryDecode' strings are different.")
        assertNull(tryDecoded.channelId, "ChannelId not null.")
    }

    private fun testEncodeDecodePadded(str: String, legacy: Boolean) {
        testPaddedEncodeDecode(str, legacy)
    }

    @Test
    fun testPaddedEncodeDecodeEmptyLegacy() {
        testEncodeDecodePadded("", true)
    }

    @Test
    fun testPaddedEncodeDecodeEmpty() {
        testEncodeDecodePadded("", false)
    }

    @Test
    fun testPaddedEncodeDecodeSingleCharLegacy() {
        testEncodeDecodePadded("a", true)
    }

    @Test
    fun testPaddedEncodeDecodeSingleChar() {
        testEncodeDecodePadded("a", false)
    }

    @Test
    fun testPaddedEncodeDecodeLongLegacy() {
        testEncodeDecodePadded("1234567890123456789012345678901234567890", true)
    }

    @Test
    fun testPaddedEncodeDecodeLong() {
        testEncodeDecodePadded("1234567890123456789012345678901234567890", false)
    }

    @Test
    fun testPaddedEncodeDecodeUnicodeLegacy() {
        testEncodeDecodePadded("\uD83D\uDE02", true)
    }

    @Test
    fun testPaddedEncodeDecodeUnicode() {
        testEncodeDecodePadded("\uD83D\uDE02", false)
    }
}