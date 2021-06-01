package by.cyberpartisan.psms.encrypted_data_encoder

class CyrillicBase64 : EncryptedDataEncoder {
    private val cyrillic = "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЫЬЭЮЯабвгдеёжзийклмнопрстуфхцчшщъыьэюя"
    private val latin = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/="

    override fun encode(data: ByteArray): String {
        val base64 = Base64().encode(data)
        return base64.map { c -> cyrillic[latin.indexOf(c)] }.toCharArray().concatToString()
    }

    override fun decode(str: String): ByteArray {
        if (!str.all { c -> cyrillic.contains(c) }) {
            throw IllegalArgumentException("string is not in valid Cyrillic Base64 scheme")
        }
        val base64 = str.map { c -> latin[cyrillic.indexOf(c)] }.toCharArray().concatToString()
        return Base64().decode(base64)
    }
}