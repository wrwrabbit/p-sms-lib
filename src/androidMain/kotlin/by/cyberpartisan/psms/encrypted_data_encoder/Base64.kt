package by.cyberpartisan.psms.encrypted_data_encoder

import android.os.Build
import java.util.Base64 as Java8Base64
import org.apache.commons.codec.binary.Base64 as ApacheBase64

actual class Base64 actual constructor() : EncryptedDataEncoder {
    actual override fun hasFrontPadding(): Boolean = false

    actual override fun encode(data: ByteArray): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Java8Base64.getEncoder().encodeToString(data)
        } else {
            ApacheBase64.encodeBase64String(data)
        }
    }

    actual override fun decode(str: String): ByteArray {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Java8Base64.getDecoder().decode(str)
        } else {
            if (!ApacheBase64.isBase64(str)) {
                throw IllegalArgumentException("Not base64 string.")
            }
            ApacheBase64.decodeBase64(str)
        }
    }
}
