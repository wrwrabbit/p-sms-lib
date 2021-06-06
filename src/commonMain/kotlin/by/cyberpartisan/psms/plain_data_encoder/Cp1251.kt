package by.cyberpartisan.psms.plain_data_encoder

class Cp1251 : PlainDataEncoder {
    private val upperCharset = "ЂЃ‚ѓ„…†‡€‰Љ‹ЊЌЋЏђ‘’“”•–—\t™љ›њќћџ ЎўЈ¤Ґ¦§Ё©Є«¬\u00AD®Ї°±Ііґµ¶·ё№є»јЅѕїАБВГДЕЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯабвгдежзийклмнопрстуфхцчшщъыьэюя"

    override fun encode(s: String): ByteArray = s.map(this::encodeChar).toByteArray()

    private fun encodeChar(char: Char): Byte {
        if (char.toInt() <= 0x7F) {
            return char.toByte()
        }
        val index = upperCharset.indexOf(char)
        return if (index != -1) (0x80 + index).toByte() else '?'.toByte()
    }

    override fun decode(data: ByteArray): String = data.map(this::decodeChar).joinToString("")

    private fun decodeChar(code: Byte): Char {
        val intCode = code.toInt() and 0xFF
        if (intCode <= 0x7F) {
            return code.toChar()
        }
        return upperCharset[intCode - 0x80]
    }

    override fun getMode(): Int = Mode.CP1251.ordinal
}
