package by.cyberpartisan.psms.plain_data_encoder

@ExperimentalUnsignedTypes
class Ascii : ShortEncoder() {
    override fun encodeChar(char: Char): Code {
        val value =  when(char.toLowerCase().toInt()) {
            in 0x00..0x7F -> char.toInt()
            else -> '?'.toInt()
        }
        return Code(value, decodingShifting)
    }

    override fun decodeChar(code: Int): Char {
        return when (code) {
            in 0x00..0x7F -> code.toChar()
            else -> '?'
        }
    }

    override fun getMode(): Int = Mode.ASCII.ordinal
}