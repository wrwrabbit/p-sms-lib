package by.cyberpartisan.psms

class PSmsCoder {

    fun encode(str: String, key: String): String {
        val pSmsEncryptor = PSmsEncryptor()
        val byteKey = md5(key.encodeToByteArray())
        val encoded = pSmsEncryptor.encode(str, byteKey, 0)
        return encoded
    }


    fun tryDecode(str: String, key: String): String {
        val pSmsEncryptor = PSmsEncryptor()
        val byteKey = md5(key.encodeToByteArray())
        val decoded = pSmsEncryptor.tryDecode(str, byteKey)
        return decoded
    }

}