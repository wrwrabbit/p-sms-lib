package by.cyberpartisan.psms.plain_data_encoder

interface PlainDataEncoder {
    fun encode(s: String): ByteArray
    fun decode(data: ByteArray): String
    fun getMode(): Int
}