package by.cyberpartisan.psms.encrypted_data_encoder

import android.os.Build
import by.cyberpartisan.psms.InvalidDataException
import java.util.Base64 as Java8Base64
import org.apache.commons.codec.binary.Base64 as ApacheBase64

actual class Base64 actual constructor() : EncryptedDataEncoder {
    actual override fun hasFrontPadding(): Boolean = false

    actual override fun encode(data: ByteArray): String {
        return ApacheBase64.encodeBase64String(data)
    }

    actual override fun decode(str: String): ByteArray {
        if (!ApacheBase64.isBase64(str)) {
            throw InvalidDataException("Not base64 string.")
        }
        return ApacheBase64.decodeBase64(str)
    }
}
