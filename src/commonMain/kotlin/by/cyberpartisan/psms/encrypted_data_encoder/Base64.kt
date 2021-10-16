package by.cyberpartisan.psms.encrypted_data_encoder

expect class Base64() : EncryptedDataEncoder {
    override fun hasFrontPadding(): Boolean
    override fun encode(data: ByteArray): String
    override fun decode(str: String): ByteArray
}
