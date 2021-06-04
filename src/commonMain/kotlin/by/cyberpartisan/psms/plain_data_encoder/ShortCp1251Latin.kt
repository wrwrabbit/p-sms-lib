package by.cyberpartisan.psms.plain_data_encoder

@ExperimentalUnsignedTypes
class ShortCp1251Latin : ShortEncoder() {
    override fun encodeChar(char: Char): Code {
        val value =  when(char.toLowerCase().toInt()) {
            in 0x20..0x7E -> char.toInt()
            in 'а'.toInt()..'я'.toInt() -> char.toLowerCase().toInt() - 'а'.toInt() // special chars
            'ё'.toInt() -> 0x7F // del
            else -> '?'.toInt()
        }
        return Code(value, decodingShifting)
    }

    override fun decodeChar(code: Int): Char {
        return when (code) {
            in 0x20..0x7E -> code.toChar()
            in 0x00..0x20 -> (code + 'а'.toInt()).toChar() // special chars
            0x7F -> 'ё' // del
            else -> '?'
        }
    }

    override fun getMode(): Int = Mode.SHORT_CP1251_PREFER_LATIN.ordinal
}