package by.cyberpartisan.psms.encryptor
import com.soywiz.krypto.AES
import kotlin.random.Random
import com.soywiz.krypto.Padding

const val IV_SIZE = 4

class AesEncryptor() : Encryptor {
    override fun encrypt(key: ByteArray, plainData: ByteArray): ByteArray {
        val ivSrc = ByteArray(IV_SIZE)
        Random.nextBytes(ivSrc)
        val iv = ivSrc + ivSrc + ivSrc + ivSrc
        return AES.encryptAesCfb(plainData, key, iv, Padding.NoPadding)
    }
    override fun decrypt(key: ByteArray, encryptedData: ByteArray): ByteArray {
        val payload = encryptedData.slice(0 until encryptedData.size - 4).toByteArray()
        val ivSrc = encryptedData.slice(encryptedData.size - 4 until encryptedData.size).toByteArray()
        val iv = ivSrc + ivSrc + ivSrc + ivSrc
        return AES.decryptAesCfb(payload, key, iv, Padding.NoPadding)
    }
}