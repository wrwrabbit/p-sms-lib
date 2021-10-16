package by.cyberpartisan.psms

import by.cyberpartisan.psms.encrypted_data_encoder.EncryptedDataEncoder
import by.cyberpartisan.psms.encrypted_data_encoder.EncryptedDataEncoderFactory
import by.cyberpartisan.psms.encrypted_data_encoder.EncryptedDataEncoderFactoryImpl
import by.cyberpartisan.psms.encrypted_data_encoder.Scheme
import by.cyberpartisan.psms.encryptor.AesEncryptor
import by.cyberpartisan.psms.encryptor.Encryptor
import by.cyberpartisan.psms.plain_data_encoder.PlainDataEncoder
import by.cyberpartisan.psms.plain_data_encoder.PlainDataEncoderFactory
import by.cyberpartisan.psms.plain_data_encoder.PlainDataEncoderFactoryImpl

const val HASH_SIZE = 2

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

    private fun pack(data: ByteArray): ByteArray {
        val hash = md5(data)
        var result = data + byteArrayOf(plainDataEncoder!!.getMode().toByte()) + hash.slice(0 until HASH_SIZE)
        if (encryptedDataEncoder!!.hasFrontPadding()) {
            result += byteArrayOf((result.size % 255).toByte())
        }
        return result
    }

    private fun unpack(data: ByteArray): ByteArray {
        var startPosition = 0
        var endPosition = data.size - HASH_SIZE - 1
        var sizeFieldSize = 0
        if (encryptedDataEncoder!!.hasFrontPadding()) {
            sizeFieldSize = 1
            endPosition--
            val actualSize = if (data.size % 255 > data.last()) data.size / 255 * 255 + data.last()
                else (data.size / 255 - 1) * 255 + data.last()
            startPosition = data.size - actualSize - sizeFieldSize
        }
        val payload = data.slice(startPosition until endPosition)
        val hash = md5(payload.toByteArray())
        if (data.slice(data.size - HASH_SIZE - sizeFieldSize until data.size - sizeFieldSize) != hash.slice(0 until HASH_SIZE))
            throw InvalidSignatureException()
        plainDataEncoder = plainDataEncoderFactory.create(data[data.size - HASH_SIZE - 1].toInt())
        return payload.toByteArray()
    }

    public fun encode(str: String, key: ByteArray, encryptionSchemeId: Int): String {
        encryptedDataEncoder = encryptedDataEncoderFactory.create(encryptionSchemeId)
        plainDataEncoder = plainDataEncoderFactory.createBestEncoder(str)
        val encoded = plainDataEncoder!!.encode(str)
        val binData = pack(encoded)
        val encryptedData = encryptor.encrypt(key, binData)
        return encryptedDataEncoder!!.encode(encryptedData)
    }

    public fun decode(str: String, key: ByteArray, encryptionSchemeId: Int): String {
        encryptedDataEncoder = encryptedDataEncoderFactory.create(encryptionSchemeId)
        val raw = encryptedDataEncoder!!.decode(str)
        val decrypted = encryptor.decrypt(key, raw)
        val unpacked = unpack(decrypted)
        return plainDataEncoder!!.decode(unpacked)
    }

    public fun isEncrypted(str: String, key: ByteArray): Boolean {
        for (scheme in Scheme.values()) {
            try {
                decode(str, key, scheme.ordinal)
                return true
            } catch (ignored: Throwable) {
            }
        }
        return false
    }

    public fun tryDecode(str: String, key: ByteArray): String {
        for (scheme in Scheme.values()) {
            try {
                return decode(str, key, scheme.ordinal)
            } catch (ignored: Throwable) {
            }
        }
        return str
    }
}