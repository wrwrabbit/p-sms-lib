package by.cyberpartisan.psms.encrypted_data_encoder

import android.os.Build
import by.cyberpartisan.psms.InvalidDataException
import java.lang.IllegalArgumentException
import android.util.Base64 as androidBase64
import org.apache.commons.codec.binary.Base64 as ApacheBase64

actual class Base64 actual constructor() : EncryptedDataEncoder {
    actual override fun hasFrontPadding(): Boolean = false

    actual override fun encode(data: ByteArray): String {
        try {
            return ApacheBase64.encodeBase64String(data)
        } catch (_: Throwable) {
            return androidBase64.encodeToString(data, androidBase64.DEFAULT)
        }

    }

    actual override fun decode(str: String): ByteArray {
        try {
            ApacheBase64.isBase64(str)
        } catch (_: Throwable) {
            try {
                return androidBase64.decode(str, androidBase64.DEFAULT)
            } catch (_: IllegalArgumentException) {
                throw InvalidDataException("Not base64 string.")
            }
        }
        if (!ApacheBase64.isBase64(str)) {
            throw InvalidDataException("Not base64 string.")
        }
        return ApacheBase64.decodeBase64(str)
    }
}
