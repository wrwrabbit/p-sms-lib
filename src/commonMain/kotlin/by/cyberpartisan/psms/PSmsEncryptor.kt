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
import com.soywiz.krypto.HMAC

const val HASH_SIZE = 2
const val CHANNEL_ID_SIZE = 4
const val VERSION = 1

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

    private fun createMetaInfo(mode: Int, isChannel: Boolean, isLegacy: Boolean): MetaInfo
        = MetaInfo(mode, if (isLegacy) 0 else VERSION, isChannel)

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

    private fun packLegacy(data: ByteArray, channelId: Int?): ByteArray {
        return pack(data, channelId, true, ::md5)
    }

    private fun pack(data: ByteArray, key: ByteArray, channelId: Int?): ByteArray {
        return pack(data, channelId, false) { d ->
            HMAC.hmacSHA256(key, d).bytes
        }
    }

    private fun pack(data: ByteArray, channelId: Int?, isLegacy: Boolean, hash: (data: ByteArray) -> ByteArray): ByteArray {
        val metaInfo = createMetaInfo(plainDataEncoder!!.getMode(), channelId != null, isLegacy)
        val channelIdBytes = if (channelId != null) intToByteArray(channelId) else byteArrayOf()
        val payload = data + channelIdBytes
        return payload + byteArrayOf(metaInfo.toByte()) + hash(payload).slice(0 until HASH_SIZE)
    }

    private fun unpackLegacy(data: ByteArray): Pair<Int?, ByteArray>{
        return unpack(data, ::md5)
    }

    private fun unpack(data: ByteArray, key: ByteArray): Pair<Int?, ByteArray> {
        return unpack(data) { d ->
            HMAC.hmacSHA256(key, d).bytes
        }
    }

    private fun unpack(data: ByteArray, hash: (data: ByteArray) -> ByteArray): Pair<Int?, ByteArray> {
        val endPosition = data.size - HASH_SIZE - 1
        if (data.size < HASH_SIZE + 1) {
            throw InvalidDataException()
        }
        val payload = data.slice(0 until endPosition).toByteArray()
        val calculatedHash = hash(payload).slice(0 until HASH_SIZE)
        val hashFromMessage = data.slice(data.size - HASH_SIZE until data.size)
        if (hashFromMessage != calculatedHash) {
            throw InvalidDataException()
        }
        val metaInfo = MetaInfo.parse(data[data.size - HASH_SIZE - 1])
        validateMetaInfo(metaInfo, payload)
        plainDataEncoder = plainDataEncoderFactory.create(metaInfo.mode)
        val channelId = if (metaInfo.isChannel)
            byteArrayToInt(payload.slice(payload.size - CHANNEL_ID_SIZE until payload.size).toByteArray())
            else null
        val textBytes = if (metaInfo.isChannel) payload.slice(0 until payload.size - 4).toByteArray() else payload
        return Pair(channelId, textBytes)
    }

    public fun encodeLegacy(message: Message, key: ByteArray, encryptionSchemeId: Int): String {
        return encodeLegacy(message, key, encryptionSchemeId, plainDataEncoderFactory.createBestEncoder(message.text))
    }

    public fun encodeLegacy(message: Message, key: ByteArray, encryptionSchemeId: Int, plainDataEncoderMode: Mode): String {
        return encodeLegacy(message, key, encryptionSchemeId, plainDataEncoderFactory.create(plainDataEncoderMode))
    }

    public fun encodeLegacy(message: Message, key: ByteArray, encryptionSchemeId: Int, plainDataEncoderMode: Int): String {
        return encodeLegacy(message, key, encryptionSchemeId, plainDataEncoderFactory.create(plainDataEncoderMode))
    }

    public fun encodeLegacy(message: Message, key: ByteArray, encryptionSchemeId: Int, plainDataEncoder: PlainDataEncoder): String {
        this.plainDataEncoder = plainDataEncoder
        encryptedDataEncoder = encryptedDataEncoderFactory.create(encryptionSchemeId)
        val encoded = plainDataEncoder.encode(message.text)
        val binData = packLegacy(encoded, message.channelId)
        val encryptedData = encryptor.encrypt(key, binData)
        return encryptedDataEncoder!!.encode(encryptedData)
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

    public fun encode(message: Message, key: ByteArray, encryptionSchemeId: Int, plainDataEncoder: PlainDataEncoder): String {
        this.plainDataEncoder = plainDataEncoder
        encryptedDataEncoder = encryptedDataEncoderFactory.create(encryptionSchemeId)
        val encoded = plainDataEncoder.encode(message.text)
        val binData = pack(encoded, key, message.channelId)
        val encryptedData = encryptor.encrypt(key, binData)
        return encryptedDataEncoder!!.encode(encryptedData)
    }

    public fun decodeLegacy(str: String, key: ByteArray, encryptionSchemeId: Int): Message {
        encryptedDataEncoder = encryptedDataEncoderFactory.create(encryptionSchemeId)
        var raw = encryptedDataEncoder!!.decode(str)
        if (encryptedDataEncoder!!.hasFrontPadding()) {
            do {
                try {
                    return decodeRawLegacy(raw, key)
                } catch (ignored: InvalidDataException) {
                }
                raw = raw.sliceArray(1 until raw.size)
            } while (raw.isNotEmpty())
            throw InvalidDataException()
        } else {
            return decodeRawLegacy(raw, key)
        }
    }

    private fun decodeRawLegacy(raw: ByteArray, key: ByteArray): Message {
        val decrypted = encryptor.decrypt(key, raw)
        val (channelId, unpacked) = unpackLegacy(decrypted)
        return Message(plainDataEncoder!!.decode(unpacked), channelId, true)
    }

    public fun decode(str: String, key: ByteArray, encryptionSchemeId: Int): Message {
        encryptedDataEncoder = encryptedDataEncoderFactory.create(encryptionSchemeId)
        var raw = encryptedDataEncoder!!.decode(str)
        if (encryptedDataEncoder!!.hasFrontPadding()) {
            do {
                try {
                    return decodeRaw(raw, key)
                } catch (ignored: InvalidDataException) {
                }
                raw = raw.sliceArray(1 until raw.size)
            } while (raw.isNotEmpty())
            throw InvalidDataException()
        } else {
            return decodeRaw(raw, key)
        }
    }

    private fun decodeRaw(raw: ByteArray, key: ByteArray): Message {
        val decrypted = encryptor.decrypt(key, raw)
        val (channelId, unpacked) = unpack(decrypted, key)
        return Message(plainDataEncoder!!.decode(unpacked), channelId)
    }

    public fun isEncrypted(str: String, key: ByteArray): Boolean {
        for (scheme in Scheme.values()) {
            try {
                decode(str, key, scheme.ordinal)
                return true
            } catch (ignored: InvalidDataException) {
            }
            try {
                decodeLegacy(str, key, scheme.ordinal)
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
            try {
                return decodeLegacy(str, key, scheme.ordinal)
            } catch (ignored: InvalidDataException) {
            }
        }
        return Message(str)
    }
}