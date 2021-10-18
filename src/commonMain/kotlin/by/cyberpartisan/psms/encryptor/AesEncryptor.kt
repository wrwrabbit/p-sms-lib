package by.cyberpartisan.psms.encryptor
import by.cyberpartisan.psms.InvalidDataException
import com.soywiz.krypto.AES
import kotlin.random.Random
import com.soywiz.krypto.Padding

const val IV_SIZE = 4

class AesEncryptor() : Encryptor {
    override fun encrypt(key: ByteArray, plainData: ByteArray): ByteArray {
        validateKey(key)
        val ivSrc = ByteArray(IV_SIZE)
        Random.nextBytes(ivSrc)
        val iv = ivSrc + ivSrc + ivSrc + ivSrc
        return AES.encryptAesCfb(plainData, key, iv, Padding.NoPadding) + ivSrc
    }

    override fun decrypt(key: ByteArray, encryptedData: ByteArray): ByteArray {
        validateKey(key)
        validateData(encryptedData)
        val payload = encryptedData.slice(0 until encryptedData.size - IV_SIZE).toByteArray()
        val ivSrc = encryptedData.slice(encryptedData.size - IV_SIZE until encryptedData.size).toByteArray()
        val iv = ivSrc + ivSrc + ivSrc + ivSrc
        return AES.decryptAesCfb(payload, key, iv, Padding.NoPadding)
    }

    private fun validateKey(key: ByteArray) {
        if (key.size !in intArrayOf(16, 24, 32)) {
            throw InvalidKeyException()
        }
    }

    private fun validateData(encryptedData: ByteArray) {
        if (encryptedData.size < IV_SIZE) {
            throw InvalidDataException()
        }
    }
}