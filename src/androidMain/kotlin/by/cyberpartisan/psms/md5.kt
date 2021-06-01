package by.cyberpartisan.psms

import java.security.MessageDigest

actual fun md5(arr: ByteArray): ByteArray {
    val digest: MessageDigest = MessageDigest.getInstance("MD5")
    digest.update(arr)
    return digest.digest()
}