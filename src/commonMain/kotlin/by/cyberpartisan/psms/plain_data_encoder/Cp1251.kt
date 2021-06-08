package by.cyberpartisan.psms.plain_data_encoder


class Cp1251() : PlainDataEncoder {
    override fun encode(s: String): ByteArray {
        return s.encodeToByteArray()
    }
    override fun decode(data: ByteArray): String {
        return data.decodeToString()
    }

    override fun getMode(): Int = Mode.CP1251.ordinal
}
