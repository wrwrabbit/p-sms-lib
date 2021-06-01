package by.cyberpartisan.psms.plain_data_encoder

import java.nio.charset.Charset

actual class Cp1251 actual constructor() : PlainDataEncoder {
    actual override fun encode(s: String): ByteArray {
        return s.toByteArray(Charset.forName("Windows-1251"))
    }

    actual override fun decode(data: ByteArray): String {
        return String(data, Charset.forName("Windows-1251"))
    }

    override fun getMode(): Int = Mode.CP1251.ordinal
}