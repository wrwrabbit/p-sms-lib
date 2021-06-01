package by.cyberpartisan.psms.plain_data_encoder

expect class Cp1251() : PlainDataEncoder {
    override fun encode(s: String): ByteArray
    override fun decode(data: ByteArray): String
}
