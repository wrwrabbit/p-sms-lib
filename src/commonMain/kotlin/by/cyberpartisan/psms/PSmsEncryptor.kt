package by.cyberpartisan.psms

import by.cyberpartisan.psms.encrypted_data_encoder.EncryptedDataEncoder
import by.cyberpartisan.psms.encrypted_data_encoder.EncryptedDataEncoderFactory
import by.cyberpartisan.psms.encrypted_data_encoder.EncryptedDataEncoderFactoryImpl
import by.cyberpartisan.psms.encrypted_data_encoder.Scheme
import by.cyberpartisan.psms.encryptor.AesEncryptor
import by.cyberpartisan.psms.encryptor.Encryptor
import by.cyberpartisan.psms.plain_data_encoder.Mode
import by.cyberpartisan.psms.plain_data_encoder.PlainDataEncoder
import by.cyberpartisan.psms.plain_data_encoder.PlainDataEncoderFactory
import by.cyberpartisan.psms.plain_data_encoder.PlainDataEncoderFactoryImpl

const val HASH_SIZE = 2
const val CHANNEL_ID_SIZE = 4
const val VERSION = 0

public class PSmsEncryptor {
    private val plainDataEncoderFactory: PlainDataEncoderFactory
    private var plainDataEncoder: PlainDataEncoder? = null

    private val encryptedDataEncoderFactory: EncryptedDataEncoderFactory
    private var encryptedDataEncoder: EncryptedDataEncoder? = null

    private var encryptor: Encryptor

    constructor() {
        plainDataEncoderFactory = PlainDataEncoderFactoryImpl()
        encryptedDataEncoderFactory = EncryptedDataEncoderFactoryImpl()
        encryptor = AesEncryptor()
    }

    constructor(plainDataEncoderFactory: PlainDataEncoderFactory,
                encryptedDataEncoderFactory: EncryptedDataEncoderFactory, encryptor: Encryptor) {
        this.plainDataEncoderFactory = plainDataEncoderFactory
        this.encryptedDataEncoderFactory = encryptedDataEncoderFactory
        this.encryptor = encryptor
    }

    private fun createMetaInfo(mode: Int, isChannel: Boolean): MetaInfo = MetaInfo(mode, VERSION, isChannel)

    private fun validateMetaInfo(metaInfo: MetaInfo, payload: ByteArray) {
        if (metaInfo.version > VERSION) {
            throw InvalidVersionException()
        }
        if (metaInfo.isChannel && payload.size < CHANNEL_ID_SIZE) {
            throw InvalidDataException()
        }
    }

    private fun intToByteArray(value: Int): ByteArray {
        return byteArrayOf(
            (value shr 0).toByte(),
            (value shr 8).toByte(),
            (value shr 16).toByte(),
            (value shr 24).toByte(),
        )
    }

    private fun byteArrayToInt(data: ByteArray): Int {
        return (data[3].toInt() shl 24) or
                (data[2].toInt() and 0xff shl 16) or
                (data[1].toInt() and 0xff shl 8) or
                (data[0].toInt() and 0xff)
    }

    private fun pack(data: ByteArray, channelId: Int?): ByteArray {
        val metaInfo = createMetaInfo(plainDataEncoder!!.getMode(), channelId != null)
        val channelIdBytes = if (channelId != null) intToByteArray(channelId) else byteArrayOf()
        val payload = data + channelIdBytes
        val hash = md5(payload)
        var result = payload + byteArrayOf(metaInfo.toByte()) + hash.slice(0 until HASH_SIZE)
        if (encryptedDataEncoder!!.hasFrontPadding()) {
            result += byteArrayOf((result.size % 255).toByte())
        }
        return result
    }

    private fun unpack(data: ByteArray): Pair<Int?, ByteArray> {
        val hasFrontPadding = encryptedDataEncoder!!.hasFrontPadding()
        var startPosition = 0
        val sizeFieldSize = if (hasFrontPadding) 1 else 0
        val endPosition = data.size - HASH_SIZE - 1 - sizeFieldSize
        if (data.size < HASH_SIZE + 1 + sizeFieldSize) {
            throw InvalidDataException()
        }
        if (hasFrontPadding) {
            val actualSize = if (data.size % 255 > data.last()) data.size / 255 * 255 + data.last()
                else (data.size / 255 - 1) * 255 + data.last()
            startPosition = data.size - actualSize - sizeFieldSize
        }
        val payload = data.slice(startPosition until endPosition).toByteArray()
        val calculatedHash = md5(payload).slice(0 until HASH_SIZE)
        val hashFromMessage = data.slice(data.size - HASH_SIZE - sizeFieldSize until data.size - sizeFieldSize)
        if (hashFromMessage != calculatedHash) {
            throw InvalidDataException()
        }
        val metaInfo = MetaInfo.parse(data[data.size - HASH_SIZE - 1 - sizeFieldSize])
        validateMetaInfo(metaInfo, payload)
        plainDataEncoder = plainDataEncoderFactory.create(metaInfo.mode)
        val channelId = if (metaInfo.isChannel)
            byteArrayToInt(payload.slice(payload.size - CHANNEL_ID_SIZE until payload.size).toByteArray())
            else null
        val textBytes = if (metaInfo.isChannel) payload.slice(0 until payload.size - 4).toByteArray() else payload
        return Pair(channelId, textBytes)
    }

    public fun encode(message: Message, key: ByteArray, encryptionSchemeId: Int): String {
        return encode(message, key, encryptionSchemeId, plainDataEncoderFactory.createBestEncoder(message.text))
    }

    public fun encode(message: Message, key: ByteArray, encryptionSchemeId: Int, plainDataEncoderMode: Mode): String {
        return encode(message, key, encryptionSchemeId, plainDataEncoderFactory.create(plainDataEncoderMode))
    }

    public fun encode(message: Message, key: ByteArray, encryptionSchemeId: Int, plainDataEncoderMode: Int): String {
        return encode(message, key, encryptionSchemeId, plainDataEncoderFactory.create(plainDataEncoderMode))
    }

    public fun encode (message: Message, key: ByteArray, encryptionSchemeId: Int, plainDataEncoder: PlainDataEncoder): String {
        this.plainDataEncoder = plainDataEncoder
        encryptedDataEncoder = encryptedDataEncoderFactory.create(encryptionSchemeId)
        val encoded = plainDataEncoder.encode(message.text)
        val binData = pack(encoded, message.channelId)
        val encryptedData = encryptor.encrypt(key, binData)
        return encryptedDataEncoder!!.encode(encryptedData)
    }

    public fun decode(str: String, key: ByteArray, encryptionSchemeId: Int): Message {
        encryptedDataEncoder = encryptedDataEncoderFactory.create(encryptionSchemeId)
        val raw = encryptedDataEncoder!!.decode(str)
        val decrypted = encryptor.decrypt(key, raw)
        val (channelId, unpacked) = unpack(decrypted)
        return Message(plainDataEncoder!!.decode(unpacked), channelId)
    }

    public fun isEncrypted(str: String, key: ByteArray): Boolean {
        for (scheme in Scheme.values()) {
            try {
                decode(str, key, scheme.ordinal)
                return true
            } catch (ignored: InvalidDataException) {
            }
        }
        return false
    }

    public fun tryDecode(str: String, key: ByteArray): Message {
        for (scheme in Scheme.values()) {
            try {
                return decode(str, key, scheme.ordinal)
            } catch (ignored: InvalidDataException) {
            }
        }
        return Message(str)
    }
}