package by.cyberpartisan.psms.plain_data_encoder

class Utf8 : PlainDataEncoder {
    override fun encode(s: String): ByteArray = s.encodeToByteArray()
    override fun decode(data: ByteArray): String = data.decodeToString()
    override fun getMode(): Int = Mode.UTF_8.ordinal
}