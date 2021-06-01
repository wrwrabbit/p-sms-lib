package by.cyberpartisan.psms.encryptor

import kotlin.random.Random
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

actual class AesEncryptor actual constructor() : Encryptor {
    actual override fun encrypt(key: ByteArray, plainData: ByteArray): ByteArray {
        val keySpec = SecretKeySpec(key, "AES")
        val ivSrc = ByteArray(IV_SIZE)
        Random.nextBytes(ivSrc)
        val iv = ivSrc + ivSrc + ivSrc + ivSrc
        val ivSpec = IvParameterSpec(iv)
        val cipher: Cipher = Cipher.getInstance("AES/CFB/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
        return cipher.doFinal(plainData) + ivSrc
    }

    actual override fun decrypt(key: ByteArray, encryptedData: ByteArray): ByteArray {
        val keySpec = SecretKeySpec(key, "AES")
        val payload = encryptedData.slice(0 until encryptedData.size - 4).toByteArray()
        val ivSrc = encryptedData.slice(encryptedData.size - 4 until encryptedData.size).toByteArray()
        val ivSpec = IvParameterSpec(ivSrc + ivSrc + ivSrc + ivSrc)
        val cipher: Cipher = Cipher.getInstance("AES/CFB/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
        return cipher.doFinal(payload)
    }
}