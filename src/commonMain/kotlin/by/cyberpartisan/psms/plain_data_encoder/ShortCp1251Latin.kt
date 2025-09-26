package by.cyberpartisan.psms.plain_data_encoder

@ExperimentalUnsignedTypes
class ShortCp1251Latin : ShortEncoder() {
    override fun encodeChar(char: Char): Code {
        val value = when (char.lowercaseChar().code) {
            in 0x20..0x7E -> char.code
            in 'а'.code..'я'.code -> char.lowercaseChar().code - 'а'.code // special chars
            'ё'.code -> 0x7F // del
            else -> '?'.code
        }
        return Code(value, decodingShifting)
    }

    override fun decodeChar(code: Int): Char {
        return when (code) {
            in 0x20..0x7E -> code.toChar()
            in 0x00..0x20 -> (code + 'а'.code).toChar() // special chars
            0x7F -> 'ё' // del
            else -> '?'
        }
    }

    override fun getMode(): Int = Mode.SHORT_CP1251_PREFER_LATIN.ordinal
}