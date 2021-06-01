package by.cyberpartisan.psms.encryptor

const val IV_SIZE = 4

expect class AesEncryptor() : Encryptor {
    override fun encrypt(key: ByteArray, plainData: ByteArray): ByteArray
    override fun decrypt(key: ByteArray, encryptedData: ByteArray): ByteArray
}