package by.cyberpartisan.psms

import by.cyberpartisan.psms.encrypted_data_encoder.EncryptedDataEncoder
import by.cyberpartisan.psms.encrypted_data_encoder.EncryptedDataEncoderFactory
import by.cyberpartisan.psms.encryptor.Encryptor
import by.cyberpartisan.psms.plain_data_encoder.PlainDataEncoder
import by.cyberpartisan.psms.plain_data_encoder.PlainDataEncoderFactory
import kotlin.test.*

@ExperimentalUnsignedTypes
class PSmsEncryptorTest {

    class PlainDataEncoderMock : PlainDataEncoder {
        override fun encode(s: String): ByteArray = s.encodeToByteArray()
        override fun decode(data: ByteArray): String = data.decodeToString()
        override fun getMode(): Int = 42 and 0x0F
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

    private fun checkEncryptedData(message: Message, data: ByteArray) {
        val encoder = PlainDataEncoderMock()
        val encodedStringSize = encoder.encode(message.text).size
        val channelIdSize = if (message.channelId != null) CHANNEL_ID_SIZE else 0
        assertEquals(encodedStringSize + 1 + HASH_SIZE + channelIdSize, data.size, "Invalid data size.")
        val decodedString = encoder.decode(data.slice(0 until encodedStringSize).toByteArray())
        assertEquals(message.text, decodedString, "Strings are not equal.")
        val payloadByteCount = message.text.encodeToByteArray().size + channelIdSize
        val calculatedMd5 = md5(data.slice(0 until payloadByteCount).toByteArray()).slice(0 until HASH_SIZE)
        val md5FromData = data.slice(data.size - HASH_SIZE until data.size)
        assertEquals(calculatedMd5, md5FromData, "Hash invalid.")
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

    private fun testEncodeDecode(str: String) {
        testEncodeDecode(Message(str))
    }

    private fun testEncodeDecode(message: Message) {
        val pSmsEncryptor = createPSmsEncryptor()
        val encoded = pSmsEncryptor.encode(message, ByteArray(0), 0)
        assertEquals(listOf(), encryptor.usedKeyEncrypt?.toList(), "Invalid used key.")
        val encryptedEncoder = EncryptedDataEncoderMock()
        checkEncryptedData(message, encryptedEncoder.decode(encoded))
        assertTrue(pSmsEncryptor.isEncrypted(encoded, ByteArray(0)), "String must be encrypted.")
        val decoded = pSmsEncryptor.decode(encoded, ByteArray(0), 0)
        assertEquals(listOf(), encryptor.usedKeyDecrypt?.toList(), "Invalid used key.")
        assertEquals(message.text, decoded.text, "Encoded and decoded strings are different.")
        assertEquals(message.channelId, decoded.channelId, "Encoded and decoded channel ids are different.")
        val tryDecoded = pSmsEncryptor.tryDecode(encoded, ByteArray(0))
        assertEquals(message.text, tryDecoded.text, "Encoded and decoded by 'tryDecode' strings are different.")
        assertEquals(message.channelId, tryDecoded.channelId, "Encoded and decoded by 'tryDecode' channel ids are different.")
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

    @Test
    fun testEncodeDecodeChannelId() {
        testEncodeDecode(Message("1234567890123456789012345678901234567890", 42))
    }

    private fun testModifiedEncrypted(modifyEncoded: (str: String) -> String) {
        val pSmsEncryptor = createPSmsEncryptor()
        val encoded = pSmsEncryptor.encode(Message("1234567890123456789012345678901234567890"), ByteArray(0), 0)
        val encodedModified = modifyEncoded(encoded)
        assertEquals(false, pSmsEncryptor.isEncrypted(encodedModified, ByteArray(0)), "Modification ignored by isEncrypted.")
        assertEquals(encodedModified, pSmsEncryptor.tryDecode(encodedModified, ByteArray(0)).text, "Modification ignored by tryDecode.")
        assertFailsWith<InvalidDataException> { pSmsEncryptor.decode(encodedModified, ByteArray(0), 0) }
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
        assertEquals(str, pSmsEncryptor.tryDecode(str, ByteArray(0)).text, "Not encrypted string ignored by tryDecode.")
        assertFails { pSmsEncryptor.decode(str, ByteArray(0), 0) }
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
        val pSmsEncryptor = PSmsEncryptor(plainFactory, encryptedFactory, EncryptorModifyVersionId())
        val encoded = pSmsEncryptor.encode(Message("1234567890123456789012345678901234567890"), ByteArray(0), 0)
        assertFailsWith<InvalidVersionException> { pSmsEncryptor.decode(encoded, ByteArray(0), 0) }
        assertFailsWith<InvalidVersionException> { pSmsEncryptor.isEncrypted(encoded, ByteArray(0)) }
        assertFailsWith<InvalidVersionException> { pSmsEncryptor.tryDecode(encoded, ByteArray(0)) }
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
        val encoded = pSmsEncryptor.encode(Message(str), ByteArray(0), 0)
        val paddingSize = paddedEncryptedFactory.encoder.paddingSize
        assertEquals(listOf(), encryptor.usedKeyEncrypt?.toList(), "Invalid used key.")
        val encryptedEncoder = EncryptedDataEncoderMock()
        checkPaddedEncryptedData(str, encryptedEncoder.decode(encoded.slice(paddingSize * 2 until encoded.length)))
        assertTrue(pSmsEncryptor.isEncrypted(encoded, ByteArray(0)), "String must be encrypted.")
        val decoded = pSmsEncryptor.decode(encoded, ByteArray(0), 0)
        assertEquals(listOf(), encryptor.usedKeyDecrypt?.toList(), "Invalid used key.")
        assertEquals(str, decoded.text, "Encoded and decoded strings are different.")
        assertNull(decoded.channelId, "ChannelId not null.")
        val tryDecoded = pSmsEncryptor.tryDecode(encoded, ByteArray(0))
        assertEquals(str, tryDecoded.text, "Encoded and decoded by 'tryDecode' strings are different.")
        assertNull(tryDecoded.channelId, "ChannelId not null.")
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