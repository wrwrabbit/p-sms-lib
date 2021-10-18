package by.cyberpartisan.psms.encrypted_data_encoder

actual class Base64 : EncryptedDataEncoder {
    actual override fun hasFrontPadding(): Boolean = false

    actual override fun encode(data: ByteArray): String {
        return com.soywiz.krypto.encoding.Base64.encode(data)
    }

    actual override fun decode(str: String): ByteArray {
        return com.soywiz.krypto.encoding.Base64.decode(str)
    }
}