package by.cyberpartisan.psms.encrypted_data_encoder

interface EncryptedDataEncoder {
    fun encode(data: ByteArray): String
    fun decode(str: String): ByteArray
}