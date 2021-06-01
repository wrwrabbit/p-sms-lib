package by.cyberpartisan.psms.encryptor

interface Encryptor {
    fun encrypt(key: ByteArray, plainData: ByteArray): ByteArray
    fun decrypt(key: ByteArray, encryptedData: ByteArray): ByteArray
}