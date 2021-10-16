package by.cyberpartisan.psms.encrypted_data_encoder

interface EncryptedDataEncoder {
    fun hasFrontPadding(): Boolean
    fun encode(data: ByteArray): String
    fun decode(str: String): ByteArray
}