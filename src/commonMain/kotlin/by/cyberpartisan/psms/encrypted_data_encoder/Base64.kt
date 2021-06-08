package by.cyberpartisan.psms.encrypted_data_encoder

import com.soywiz.krypto.encoding.Base64

class Base64: EncryptedDataEncoder {
    override fun encode(data: ByteArray): String {
        return Base64.encode(data)
    }

    override fun decode(str: String): ByteArray {
        return Base64.decode(str)
    }
}
